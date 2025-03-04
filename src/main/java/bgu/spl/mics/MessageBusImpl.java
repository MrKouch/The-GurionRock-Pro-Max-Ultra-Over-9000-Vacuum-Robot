package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.services.FusionSlamService;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
    
    // Fields
    private ConcurrentHashMap<Class<? extends Event<?>>, LinkedBlockingQueue<MicroService>> eventsSubscribers;
    private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> broadcastsSubscribers;
    private ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> microServicesMessages;
    private ConcurrentHashMap<Event<?>, Future<?>> eventsFutures;


    private MessageBusImpl() {
        eventsSubscribers = new ConcurrentHashMap<>();
        broadcastsSubscribers = new ConcurrentHashMap<>();
        microServicesMessages = new ConcurrentHashMap<>();
        eventsFutures = new ConcurrentHashMap<>();
    }

    private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    // public getInstance method
    public static MessageBusImpl getMessageBus() {
        return MessageBusHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        eventsSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
        eventsSubscribers.get(type).add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        broadcastsSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
        broadcastsSubscribers.get(type).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) eventsFutures.get(e);
        if(future != null) {
            future.resolve(result);
        }
        eventsFutures.remove(e);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Iterator<MicroService> bSubscribers = broadcastsSubscribers.get(b.getClass()).iterator();
        while (bSubscribers.hasNext()) {
            MicroService m = bSubscribers.next();
            synchronized (microServicesMessages) {
                if (microServicesMessages.get(m) != null) {
                    if (b instanceof CrashedBroadcast || (b instanceof TerminatedBroadcast && ((TerminatedBroadcast)b).getServiceWhoTerminated() == FusionSlamService.class))
                    microServicesMessages.get(m).addFirst(b);
                    else {
                        microServicesMessages.get(m).addLast(b);
                    }   
                }
            }
        }  
        
    }

    
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        synchronized (eventsSubscribers) {
            MicroService m = eventsSubscribers.get(e.getClass()).remove();
            eventsSubscribers.get(e.getClass()).add(m);
            synchronized (microServicesMessages) {
                if (microServicesMessages.get(m) != null)
                    microServicesMessages.get(m).addLast(e);
            }
        }
        Future<T> future = new Future<T>();
        eventsFutures.put(e, future);
        return future;
    }

    @Override
    public void register(MicroService m) {
        LinkedBlockingDeque<Message> mailBox = new LinkedBlockingDeque<Message>();
        microServicesMessages.put(m, mailBox);
    }

    @Override
    public void unregister(MicroService m) {
        microServicesMessages.remove(m);
        synchronized (eventsSubscribers) {
            Enumeration<Class<? extends Event<?>>> eventsEnu = eventsSubscribers.keys();
            while (eventsEnu.hasMoreElements()) { 
                LinkedBlockingQueue<MicroService> currentQ = eventsSubscribers.get(eventsEnu.nextElement());
                currentQ.remove(m);
            }
        }
        
        Enumeration<Class<? extends Broadcast>> broadcastsEnu = broadcastsSubscribers.keys();
        while (broadcastsEnu.hasMoreElements()) { 
            LinkedBlockingQueue<MicroService> currentQ = broadcastsSubscribers.get(broadcastsEnu.nextElement());
            currentQ.remove(m);
        }
            microServicesMessages.remove(m); // Calling it again, in case more broadcasts calls were added until m was removed from the subscribers queue
        
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        LinkedBlockingDeque<Message> queue = microServicesMessages.get(m);
        if(queue == null) {
            throw new IllegalStateException("MicroService is not registered");
        }
        try {
            return queue.take();
        } 
        catch (InterruptedException e) {
            throw new InterruptedException();
        }
    }

    // @PRE: none
    // @POST: trivial (simple getter)
    public ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> getRegisteredMS() {
        return microServicesMessages;
    }
    
    // @PRE: none
    // @POST: trivial (simple getter)
    public ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> getBroadcastsSubscribers() {
        return broadcastsSubscribers;
    }
}