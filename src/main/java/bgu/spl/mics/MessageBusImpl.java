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
    // private ReentrantReadWriteLock microServicesReadWriteLock;
    // private ReentrantReadWriteLock eventsReadWriteLock;
    // private ReentrantReadWriteLock broadcastsReadWriteLock;


    private MessageBusImpl() {
        eventsSubscribers = new ConcurrentHashMap<>();
        broadcastsSubscribers = new ConcurrentHashMap<>();
        microServicesMessages = new ConcurrentHashMap<>();
        eventsFutures = new ConcurrentHashMap<>();
        // microServicesReadWriteLock = new ReentrantReadWriteLock();
        // eventsReadWriteLock = new ReentrantReadWriteLock();
        // broadcastsReadWriteLock = new ReentrantReadWriteLock();
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
        eventsSubscribers.get(type).add(m); // MAKE SURE: Can we assume that m is not in the queue right now?
		
		// Its better to use computeIfAbsent
		// IMPROVEMENT SUGGESTION

		// eventsSubscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<MicroService>()).add((m));
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        broadcastsSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
        broadcastsSubscribers.get(type).add(m); // MAKE SURE: Can we assume that m is not in the queue right now?
		// Its better to use computeIfAbsent
		// IMPROVEMENT SUGGESTION
		// broadcastsSubscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<MicroService>()).add((m));
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
        locker.writeLock().lock();
        try {
            Iterator<MicroService> bSubscribers = broadcastsSubscribers.get(b.getClass()).iterator();
            while (bSubscribers.hasNext()) {
                MicroService m = bSubscribers.next();
                if (b instanceof CrashedBroadcast || (b instanceof TerminatedBroadcast && ((TerminatedBroadcast)b).getServiceWhoTerminated() == FusionSlamService.class))
                    microServicesMessages.get(m).addFirst(b);
                else {
                    if (microServicesMessages.get(m) == null) {
                        System.out.println("m: " + m);
                        System.out.println("microServicesMessages.get(m) null");
                    }
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
            //sync - unlock
            Future<T> future = new Future<T>();
            eventsFutures.put(e, future);
            return future; // MAKE SURE
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
        return queue.take(); // Blocks until a message is available
    }

    public ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> getRegisteredMS() {
        return microServicesMessages;
    }
    
    public ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> getBroadcastsSubscribers() {
        return broadcastsSubscribers;
    }
}



