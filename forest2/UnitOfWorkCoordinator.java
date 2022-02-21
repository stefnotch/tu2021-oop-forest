package forest2;

import java.util.concurrent.locks.ReentrantLock;

// Executes operations on a shared datastructure in a guaranteed safe manner
// If we wanna make it generic:
// T extends UnitOfWork
// U = shared datastructure
public class UnitOfWorkCoordinator {

    private static class LinkedListNode {
        LinkedListNode previous;
        final UnitOfWork unitOfWork;
        final ReentrantLock lock;
        boolean completed; // so that we can eventually remove this node from the list

        public LinkedListNode(LinkedListNode previous, UnitOfWork unitOfWork, ReentrantLock lock) {
            this.previous = previous;
            this.unitOfWork = unitOfWork;
            this.lock = lock;
        }
    }

    private LinkedListNode tail;
    private final Forest forest;

    public UnitOfWorkCoordinator(Forest forest) {
        this.forest = forest;
    }

    public void addAndExecute(UnitOfWork unitOfWork) {
        if(unitOfWork == null) {
            throw new IllegalArgumentException();
        }
        LinkedListNode node = addUnitOfWork(unitOfWork);
        waitForDependencies(node);
        try {
            unitOfWork.getStep().accept(forest);
        } finally {
            release(node);
        }
    }

    private synchronized LinkedListNode addUnitOfWork(UnitOfWork unitOfWork) {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        LinkedListNode newTail = new LinkedListNode(tail, unitOfWork, lock);
        tail = newTail;
        return newTail;
    }

    private void waitForDependencies(LinkedListNode node) {
        LinkedListNode current = node.previous;
        while(current != null && current.completed) { // get rid of *some* useless nodes. TODO: Get rid of all useless nodes
            node.previous = current.previous; // node.previous doesn't have to be volatile, since the old value is still useable
            current = current.previous;
        }
        while (current != null) {
            if(node.unitOfWork.dependsOnAccept(current.unitOfWork)) {
                current.lock.lock();
                current.lock.unlock();
            }
            current = current.previous;
        }
    }

    private void release(LinkedListNode node) {
        // TODO: Setting previous to null doesn't quite work, it ignores some important dependencies
        // node.previous = null; // for the GC
        node.completed = true;
        node.lock.unlock();
    }
}
