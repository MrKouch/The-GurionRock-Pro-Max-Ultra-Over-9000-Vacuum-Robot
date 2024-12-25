package bgu.spl.mics;
import java.util.*;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	
	// Fields
	private static volatile MessageBus theBus;
	private HashMap<Event<?>, Queue<MicroService>> eventsMap;
	private HashMap<Broadcast, Queue<MicroService>> broadcastsMap;

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	// getters
	// initializes the bus in the first call, and returns the existing one for all later calls.
	public static MessageBus getMessageBus() {
		if(theBus == null) {
			synchronized(MessageBus.class) {
				if(theBus == null)
				theBus = new MessageBusImpl();
			}
		}
		return theBus;
	}
}
