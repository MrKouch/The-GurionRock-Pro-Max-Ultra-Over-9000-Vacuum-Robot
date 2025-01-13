package bgu.spl.mics;

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
    
    private ReentrantReadWriteLock locker;


    private MessageBusImpl() {
        eventsSubscribers = new ConcurrentHashMap<>();
        broadcastsSubscribers = new ConcurrentHashMap<>();
        microServicesMessages = new ConcurrentHashMap<>();
        eventsFutures = new ConcurrentHashMap<>();
        locker = new ReentrantReadWriteLock();

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
        Future<T> future = (Future<T>) eventsFutures.get(e); // Handle this warning
        if(future != null) {
            future.resolve(result);
        }
        eventsFutures.remove(e);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        locker.writeLock().lock();
        try {
            Iterator<MicroService> bSubscribers = broadcastsSubscribers.get(b.getClass()).iterator();
            while (bSubscribers.hasNext()) {
                MicroService m = bSubscribers.next();
                if (b instanceof CrashedBroadcast || (b instanceof TerminatedBroadcast && ((TerminatedBroadcast)b).getServiceWhoTerminated() == FusionSlamService.class))
                    microServicesMessages.get(m).addFirst(b);
                else {
                    microServicesMessages.get(m).addLast(b);
                }
            }  
        } finally {
            locker.writeLock().unlock();
        }
        
    }

    
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        locker.writeLock().lock();
        try {
            MicroService m = eventsSubscribers.get(e.getClass()).remove();
            microServicesMessages.get(m).addLast(e);
            eventsSubscribers.get(e.getClass()).add(m);
            Future<T> future = new Future<T>();
            eventsFutures.put(e, future);
            return future;
        } finally {
            locker.writeLock().unlock();
        }
    }

    @Override
    public void register(MicroService m) {
        LinkedBlockingDeque<Message> mailBox = new LinkedBlockingDeque<Message>();
        microServicesMessages.put(m, mailBox);
    }

    @Override
    public void unregister(MicroService m) {
        locker.writeLock().lock();
        try {
            microServicesMessages.remove(m);
            Enumeration<Class<? extends Event<?>>> eventsEnu = eventsSubscribers.keys();
            while (eventsEnu.hasMoreElements()) { 
                LinkedBlockingQueue<MicroService> currentQ = eventsSubscribers.get(eventsEnu.nextElement());
                currentQ.remove(m);
            }
            
            Enumeration<Class<? extends Broadcast>> broadcastsEnu = broadcastsSubscribers.keys();
            while (broadcastsEnu.hasMoreElements()) { 
                LinkedBlockingQueue<MicroService> currentQ = broadcastsSubscribers.get(broadcastsEnu.nextElement());
                currentQ.remove(m);
            }
        } finally {
            locker.writeLock().unlock();
        }
        
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        LinkedBlockingDeque<Message> queue = microServicesMessages.get(m);
		if(queue == null) {
			throw new IllegalStateException("MicroService is not registered");
		}
        return queue.take();
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



