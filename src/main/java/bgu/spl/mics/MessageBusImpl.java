package bgu.spl.mics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
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
    private static volatile MessageBus theBus;
    private ConcurrentHashMap<Class<? extends Event<?>>, LinkedBlockingQueue<MicroService>> eventsSubscribers;
    private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> broadcastsSubscribers;
    private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microServicesMessages;
    private ConcurrentHashMap<Event<?>, Future<?>> eventsFutures;

    private MessageBusImpl() {
        eventsSubscribers = new ConcurrentHashMap<>();
        broadcastsSubscribers = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        // eventsSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
        // eventsSubscribers.get(type).add(m); // MAKE SURE: Can we assume that m is not in the queue right now?
		
		// Its better to use computeIfAbsent
		// IMPROVEMENT SUGGESTION
		eventsSubscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<MicroService>()).add((m));
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        // broadcastsSubscribers.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
        // broadcastsSubscribers.get(type).add(m); // MAKE SURE: Can we assume that m is not in the queue right now?

		// Its better to use computeIfAbsent
		// IMPROVEMENT SUGGESTION
		broadcastsSubscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<MicroService>()).add((m));
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) eventsFutures.get(e); // Handle this warning
        future.resolve(result);
        eventsFutures.remove(e);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Iterator<MicroService> bSubscribers = broadcastsSubscribers.get(b.getClass()).iterator();
        while (bSubscribers.hasNext())  
            microServicesMessages.get(bSubscribers.next()).add(b);
    }

    
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        MicroService m = eventsSubscribers.get(e.getClass()).remove();
        microServicesMessages.get(m).add(e);
        eventsSubscribers.get(e.getClass()).add(m);

        Future<T> future = new Future<T>();
        eventsFutures.put(e, future);
        return future; // MAKE SURE
    }

    @Override
    public void register(MicroService m) {
        microServicesMessages.put(m, new LinkedBlockingQueue<Message>());
    }

    @Override
    public void unregister(MicroService m) {
        microServicesMessages.remove(m);

        Enumeration<Class<? extends Event<?>>> enu = eventsSubscribers.keys();
        // Displaying the Enumeration 
        while (enu.hasMoreElements()) { 
            System.out.println(enu.nextElement()); 
        } 

        // Iterator<ConcurrentHashMap.Entry<Class<? extends Event<?>>, LinkedBlockingQueue<MicroService>>>itr =
        //  eventsSubscribers.entrySet().iterator();
        // while (itr.hasNext()) {
        //     ConcurrentHashMap.Entry<Class<? extends Event<?>>, LinkedBlockingQueue<MicroService>> entry
        //         = itr.next();
        //  eventsSubscribers.get(entry).remove(entry);
        // }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        LinkedBlockingQueue<Message> queue = microServicesMessages.get(m);
		if(queue == null) {
			throw new IllegalStateException("MicroService is not registered");
		}
        return queue.take(); // Blocks until a message is available
    }

    // 
    // public getInstance method
    public static MessageBus getMessageBus() {
        if(theBus == null) {
            synchronized(MessageBus.class) {
                if(theBus == null)
                theBus = new MessageBusImpl();
            }
        }
        return theBus;
    }

	// WHAT IS THIS?
    public static MessageBus removeFromQueue() {
        if(theBus == null) {
            synchronized(MessageBus.class) {
                if(theBus == null)
                theBus = new MessageBusImpl();
            }
        }
        return theBus;
    }

}



