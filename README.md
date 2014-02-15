# PatrollingProblem

For a basic simulation, run `Simulation.java`.
For CSV data, run `SimulationManager.java`.


SimulationManager is used to create a wide set of Simulations based off of varying parameters. Data is then collected from the Simulations and stored as a CSV file. Simulations are also graphed.

There are multiple parameters involved in each Simulation. A simulation is based off of an abstract sense of time that we call a 'tick'. 

Events are generated at a constant rate or an exponential rate. Constant event generation ranges from 1 to 5.5 events generated per 'tick' while exponential event generation is based off of an exponential distribution with mean ranging from 1 to 5.5. When an event is generated, the value of that event is a random number from 1 to 10.

The value or priority of an event can either remove constant or it can decrease. When the event value is constant, its value never changes. When an event's value is set to decrease, the event's priority value decreases by 1 value every two ticks.

For each simulation, all agents will share a service rate. The service rate is the speed at which an agent traverses an edge. The traversal speed is directly proportional to the value of an edge. For our simulations, the service rates vary from f/10, f/20, and f/50. For example, if the service rate is f/10, and the edge has a value of 15, then the agent will spend 1.5 'ticks' traversing that edge.

When an edge has no value, an agent is said to 'idle' across the edge. Idle time is equal to the traversal time of an edge with a value of 1.

The number of agents vary from 1 to 4.
 
A simulation will end after 100,000 events have been generated.

 