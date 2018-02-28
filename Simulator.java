import java.util.PriorityQueue;
import java.util.Comparator;
import java.lang.Math;
import java.util.Random;

/**
 * The Simulator class encapsulates information and methods pertaining to a
 * Simulator.
 *
 * @author Ooi Wei Tsang
 * @author Evan Tay
 * @version CS2030 AY17/18 Sem 2 Lab 1b
 */
class Simulator {


  /** The array of events. Events may not be ordered according to time . */
  private Comparator<Event> comparator = new TimeOfEventComparator();
  private PriorityQueue<Event> events;
  public RandomGenerator random;
  /** The shop of servers being simulated. */
  private final Shop shop;
  private int numCust;
  private double arrivalTime = 0;

  /** The statistics being maintained. */
  public static Statistics stats = new Statistics();

  /**
   * Create a Simulator and initializes it.
   *
   * @param numOfServers Number of servers to be created for simulation.
   */
  public Simulator(int numOfServers, int numOfCustomers, int seed, double lambda, double mu) {
    this.shop = new Shop(numOfServers);
    this.random = new RandomGenerator(seed, lambda, mu);
    this.events = new PriorityQueue<Event> (numOfCustomers*2, comparator);
    this.numCust = numOfCustomers;
  }

  /**
   * Schedules an event with this simulator. The simulator maintains an array
   * of events (in arbitrary order). This method appends the given event at
   * the end of the array.
   *
   * @param event The event to be scheduled for simulation.
   * @return Returns true if the event is added successfully; false otherwise.
   */
  public void scheduleEvent(Event event) {
    events.add(event);
  }

  /**
   * Run the Simulator until all scheduled events are simulated.
   */
  public void run() {
    for (int i = 0; i < this.numCust; i++) {
      this.createArrivalEvent(arrivalTime);    
      arrivalTime += this.random.genInterArrivalTime();
      while (events.peek() != null && events.peek().time <= arrivalTime) {
        events.poll().simulate(this);      
      }
    }

    while (events.peek() != null) {
      events.poll().simulate(this);      
    }


  }

  /**
   * Create a DoneEvent for the given server and customer to be simulated.
   *
   * @param time The time this event is scheduled to occur.
   * @param server The server to serve the customer.
   * @param customer The customer being served.
   * @return true if event is scheduled successfully, false otheriwise.
   */
  public void createDoneEvent(double time, Server server, Customer customer) {
    Event event = new DoneEvent(time + this.random.genServiceTime(),
        server, customer);
    scheduleEvent(event);
  }

  /**
   * Create an ArrivalEvent to mark the arrival of a customer.
   *
   * @param time The time this event is scheduled to occur.
   * @return true if event is scheduled successfully, false otheriwise.
   */
  public void createArrivalEvent(double time) {
    Event event = new ArrivalEvent(time);
    this.scheduleEvent(event);
  }

  /**
   * Simulate the arrival of a customer.
   *
   * @param time The time this event is scheduled to occur.
   */
  public void simulateArrival(double time) {
    Customer customer = new Customer();
    customer.arrive(time);

    Server server = shop.findIdleServer();
    if (server != null) {
      this.serveCustomer(time, server, customer);
      return;
    }

    // If server with no customer waiting is found, wait for this server
    server = shop.findServerWithNoWaitingCustomer();
    if (server != null) {
      this.makeCustomerWait(time, server, customer);
      return;
    }

    // If idle server not found, and if server with no customer waiting not
    // found, customer leaves (maximum of one waiting customer per server).
    this.customerLeaves(time, customer);
  }

  /**
   * Simulate that the server is done serving a customer.
   *
   * @param time The time this event is scheduled to occur.
   * @param server The server to serve the customer.
   * @param customer The customer being served.
   */
  public void simulateDone(double time, Server server, Customer customer) {
    customer.serveEnd(time, server);
    if (server.customerWaiting()) {
      // Someone is waiting, serve this waiting someone
      this.serveWaitingCustomer(time, server);
    } else {
      // Server idle
      server.makeIdle();
    }
  }

  /**
   * Simulate a server starting to serve a customer at the given time.
   * Precondition: No one else must be served at this time.
   *
   * @param time The {@code time} at which the {@code server} serves the
   *     {@code customer}.
   * @param server The {@code server} who would be serving the {@code customer}
   *     at {@code time}.
   * @param customer The {@code customer} to be served by the {@code server} at
   *     {@code time}.
   */
  public void serveCustomer(double time, Server server, Customer customer) {
    server.serve(customer);
    customer.serveBegin(time, server);
    this.createDoneEvent(time, server, customer);
  }

  /**
   * Simulate a customer starting to wait for a server at time.
   * Precondition: All customers are busy serving and there is at least one
   * server with no customer waiting for him.
   *
   * @param time The {@code time} at which the {@code customer} starts
   *     waiting for the {@code server}.
   * @param server The server whom the customer is waiting for at time.
   * @param customer The customer who is waiting for the server at time.
   */
  public void makeCustomerWait(double time, Server server, Customer customer) {
    server.askToWait(customer);
    customer.waitBegin(time, server);
  }

  /**
   * Simulate a server serving his waiting customer at time.
   *
   * @param time The {@code time} at which the {@code server} serves
   *     his waiting customer.
   * @param server The {@code server} who is to serve his waiting customer.
   */
  public void serveWaitingCustomer(double time, Server server) {
    Customer customer = server.removeWaitingCustomer();
    this.serveCustomer(time, server, customer);
  }

  /**
   * Simulate a customer leaving because all servers are busy serving and
   * every server has a customer waiting.
   *
   * @param time The {@code time} at which the {@code customer} leaves.
   * @param customer The {@code customer} which leaves at {@code time}.
   */
  public void customerLeaves(double time, Customer customer) {
    customer.leave(time);
  }
}
