package engine;


import engine.exception.BPSimulatorException;
import interfaces.IEventProcessor;
import interfaces.IProcessLogger;
import interfaces.IResourceManager;
import model.ProcessActivity;
import model.Resource;
import model.TaskResource;

import java.util.*;

/**
 * Resource Manager component to handle resourcing in simulation
 */
public class ResourceManager implements IResourceManager {
    private BPSimulator simInstance;
    private IEventProcessor eventProcessor;
    private IProcessLogger processLogger;

    //
    private Map<Integer, Queue<ProcessActivity>> pendingActivities = new HashMap();

    //resources allocated to activities (key - activity id, value - list of resources )
    private Map<Integer, ResourceManager.ResourceList> resourceAllocations = new HashMap();
    private Map<String, Resource> definedResources = new HashMap();
    private ResourceManager.ResourceList[] resourcePool;


    private double[] resourceLastWorkStartTime;

    //statistics for every resource (index - id of resource)
    private double[] resourceTotalWorkedTime;

    private Integer resourceIndex = 0;

    public ResourceManager(BPSimulator simulationInstance) {
        this.simInstance = simulationInstance;
    }

    public void initComponent() {
        this.resourcePool = new ResourceManager.ResourceList[this.definedResources.size()];
        int uniqueResourceId = 0;

        for(Resource resource:definedResources.values())
         {
            ResourceManager.ResourceList pool = new ResourceManager.ResourceList();
            this.resourcePool[resource.getIndex()] = pool;

            for(int i = 0; i < resource.getTotalAmount(); ++i) {
                pool.add(new TaskResource(resource, uniqueResourceId++));
            }
        }

        this.resourceTotalWorkedTime = new double[uniqueResourceId];
        this.resourceLastWorkStartTime = new double[uniqueResourceId];
    }

    public void notifyActivitiesEnabled(ProcessActivity[] enabledActivities) throws BPSimulatorException {
        for(int i = 0; i < enabledActivities.length; ++i) {
            if (enabledActivities[i] != null) {
                Resource r = enabledActivities[i].getActivity().getResource();

                enabledActivities[i].stamp(ProcessActivity.ENABLED, this.simInstance.getClock().getTime());
                if (r != null) {
                    int available = this.getAvailableResources(r);
                    if (available == 0) { //if resource is not available
                        this.enqueueActivity(enabledActivities[i]); // put activity in queue
                        this.processLogger.logResourceUnavailable(enabledActivities[i]);
                        enabledActivities[i] = null; //activity is not execute
                    } else {
                        Integer index = r.getIndex();
                        this.processLogger.logResourceAvailable(enabledActivities[i], available);
                        // if resource is available, then assign it
                        this.assignResourceToActivity(enabledActivities[i], index);
                    }
                }
            }
        }

        // tell to event processor that activity is processing
        this.eventProcessor.notifyStartedActivities(enabledActivities);
    }


    /**
     *
     * @param processActivity
     * @param resourceIndex
     */
    private void assignResourceToActivity(ProcessActivity processActivity, Integer resourceIndex) {
        Integer processId = processActivity.getProcessInstance().getId();
        ResourceManager.ResourceList processMap = (ResourceManager.ResourceList)this.resourceAllocations.get(processId);
        if (processMap == null) {
            processMap = new ResourceManager.ResourceList();
            this.resourceAllocations.put(processId, processMap);
        }

        TaskResource taskResource = this.getResourcePool(resourceIndex).poll();
        processActivity.setAssignedResource(taskResource);
        processMap.add(taskResource);
        this.resourceLastWorkStartTime[taskResource.getId()] = this.simInstance.getClock().getTime();
    }

    /**
     * Notify the resource manager that resources used by activity are available
     * @param processActivity - activity
     * @throws BPSimulatorException
     */
    public void notifyResourceAvailableFromActivity(ProcessActivity processActivity) throws BPSimulatorException {
        TaskResource freeResource = processActivity.getAssignedResource();
        //free resource by removing it from allocation list
        (this.resourceAllocations.get(processActivity.getProcessInstance().getId())).remove(freeResource);

        double[] var10000 = this.resourceTotalWorkedTime;
        int var10001 = freeResource.getId();
        var10000[var10001] += processActivity.getDuration();
        Integer resourceIndex = freeResource.getResource().getIndex();
        this.resourcePool[resourceIndex].add(freeResource);
        this.startQueuedActivitiesForResource(freeResource.getResource());
    }






    public void notifyResourcesAvailableFromProcess(Integer processIndex) throws BPSimulatorException {
        ResourceManager.ResourceList processMap = (ResourceManager.ResourceList)this.resourceAllocations.get(processIndex);
        if (processMap != null) {
            while(true) {
                if (processMap.isEmpty()) {
                    this.resourceAllocations.remove(processIndex);
                    break;
                }

                TaskResource freeResource = processMap.poll();
                double workTime = this.simInstance.getClock().getTime() - this.resourceLastWorkStartTime[freeResource.getId()];
                if (freeResource.getResource().getTimeTable() != null) {
                    workTime -= freeResource.getResource().getTimeTable().getTotalIdleTime(this.resourceLastWorkStartTime[freeResource.getId()], this.simInstance.getClock().getTime());
                }

                double[] var10000 = this.resourceTotalWorkedTime;
                int var10001 = freeResource.getId();
                var10000[var10001] += workTime;
                Integer resourceIndex = freeResource.getResource().getIndex();
                this.getResourcePool(resourceIndex).add(freeResource);
                this.startQueuedActivitiesForResource(freeResource.getResource());
            }
        }

    }

    private void startQueuedActivitiesForResource(Resource resource) throws BPSimulatorException {
        Integer resourceIndex = resource.getIndex();
        if (this.hasQueuedActivities(resourceIndex)) {
            Queue<ProcessActivity> q = this.getResourceQueue(resourceIndex);
            ProcessActivity[] started = new ProcessActivity[this.getAvailableResources(resource)];
            int i = 0;

            while(!q.isEmpty() && i < started.length) {
                if (!((ProcessActivity)q.peek()).isEnabled()) {
                    this.processLogger.logElementWithdrawn((ProcessActivity)q.remove());
                } else {
                    started[i] = (ProcessActivity)q.poll();
                    this.assignResourceToActivity(started[i], resourceIndex);
                    ++i;
                }
            }

            if (i > 0) {
                this.eventProcessor.notifyStartedActivities(started);
                return;
            }
        }

    }

    public IEventProcessor getEventProcessor() {
        return this.eventProcessor;
    }

    public void setEventProcessor(IEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public IProcessLogger getProcessLogger() {
        return this.processLogger;
    }

    public void setProcessLogger(IProcessLogger processLogger) {
        this.processLogger = processLogger;
    }

    public void defineResource(Resource resource) {
        Integer var2 = this.resourceIndex;
        Integer var3 = this.resourceIndex = this.resourceIndex + 1;
        resource.setIndex(var2);
        this.definedResources.put(resource.getId(), resource);
    }

    private void enqueueActivity(ProcessActivity processActivity) {
        this.getResourceQueue(processActivity.getActivity().getResource().getIndex()).add(processActivity);
    }

    private boolean hasQueuedActivities(Integer resourceIndex) {
        Queue<ProcessActivity> queue = (Queue)this.pendingActivities.get(resourceIndex);
        return queue != null && !queue.isEmpty();
    }

    public int getAvailableResources(Resource resource) {
        return this.resourcePool[resource.getIndex()].count();
    }

    private Queue<ProcessActivity> getResourceQueue(Integer resourceIndex) {
        Queue<ProcessActivity> queue = (Queue)this.pendingActivities.get(resourceIndex);
        if (queue == null) {
            queue = new LinkedList();
            this.pendingActivities.put(resourceIndex, queue);
        }

        return (Queue)queue;
    }

    public Resource getDefinedResource(String id) {
        return (Resource)this.definedResources.get(id);
    }

    public Collection<Resource> getDefinedResources() {
        return this.definedResources.values();
    }

    private ResourceManager.ResourceList getResourcePool(Integer resourceIndex) {
        return this.resourcePool[resourceIndex];
    }

    public double getResourceUtilization(Resource resource, double simulationStart, double simulationEnd) {
        if (resource.getTotalAmount() == 0) {
            return 0.0D;
        } else {
            double utilization = 0.0D;
            double simulationDuration = simulationEnd - simulationStart;
            ResourceManager.ResourceList pool = this.resourcePool[resource.getIndex()];
            TaskResource res = pool.peek();
            if (res.getResource().getTimeTable() != null) {
                simulationDuration -= res.getResource().getTimeTable().getTotalIdleTime(simulationStart, simulationEnd);
            }

            while(res != null) {
                utilization += this.resourceTotalWorkedTime[res.getId()] / simulationDuration;
                res = res.getNext();
            }

            return resource.getTotalAmount() == 0 ? 0.0D : utilization / (double)resource.getTotalAmount();
        }
    }

    private class ResourceList {
        private TaskResource first;
        private TaskResource last;
        private int count;

        private ResourceList() {
            this.count = 0;
        }

        public void add(TaskResource resource) {
            if (this.last == null) {
                this.first = resource;
                this.last = resource;
            } else {
                this.last.setNext(resource);
                this.last = resource;
                resource.setNext((TaskResource)null);
            }

            ++this.count;
        }

        public TaskResource poll() {
            TaskResource res = this.first;
            if (res != null) {
                this.first = res.getNext();
                if (res == this.last) {
                    this.last = null;
                }

                res.setNext((TaskResource)null);
            }

            --this.count;
            return res;
        }

        public void remove(TaskResource resource) {
            if (resource == this.first) {
                this.first = this.first.getNext();
                if (resource == this.last) {
                    this.last = null;
                }
            } else {
                TaskResource res;
                for(res = this.first; res.getNext() != resource; res = res.getNext()) {
                    ;
                }

                if (res.getNext() == this.last) {
                    this.last = res;
                }

                res.setNext(res.getNext().getNext());
            }

            --this.count;
        }

        public boolean isEmpty() {
            return this.first == null;
        }

        public int count() {
            return this.count;
        }

        public TaskResource peek() {
            return this.first;
        }
    }
}
