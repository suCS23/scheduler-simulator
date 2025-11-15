public class queue {

    private node front;
    private node rear;
    private int size;
    private String schType;  // "fifo", "rm" (requested memory), "scheduler"

    queue(String schType) {
        this.schType = schType;
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    void enqueue(process p) {
        node newNode = new node(p, null);
        
        // HQ1 uses "rm" - sorted ascending by memory required
        if (schType.equals("rm")) {
            // Insert in sorted order by memory (ascending)
            if (isEmpty() || front.p.getMr() > p.getMr()) {
                // Insert at front
                newNode.next = front;
                front = newNode;
                if (rear == null) {
                    rear = front;
                }
            } else if (front.p.getMr() == p.getMr()) {
                // Same memory - FIFO for ties (insert after existing)
                node current = front;
                while (current.next != null && current.next.p.getMr() <= p.getMr()) {
                    current = current.next;
                }
                newNode.next = current.next;
                current.next = newNode;
                if (newNode.next == null) {
                    rear = newNode;
                }
            } else {
                // Find correct position
                node current = front;
                while (current.next != null && current.next.p.getMr() < p.getMr()) {
                    current = current.next;
                }
                newNode.next = current.next;
                current.next = newNode;
                if (newNode.next == null) {
                    rear = newNode;
                }
            }
        } else {
            // FIFO for ready queue, HQ2, and submit queue
            if (isEmpty()) {
                front = newNode;
                rear = front;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
        }
        size++;
    }

    node dequeue() {
        if (!isEmpty()) {
            node temp = front;
            front = front.next;
            if (front == null) {
                rear = null;
            }
            size--;  // FIX: decrement size
            return temp;
        }
        return null;
    }

    void explode() {
        while (!isEmpty()) {
            front = front.next;
            size--;
        }
        rear = null;
        size = 0;
    }

    node peek() { 
        return (!isEmpty()) ? front : null;
    }

    boolean isEmpty() {
        return front == null;
    }

    int getSize() {
        return size;
    }
    
    // Helper to get front node for iteration (needed for display)
    node getFront() {
        return front;
    }
}

class node {
    public process p;
    public node next;

    public node(process p, node next) {
        this.p = p;
        this.next = next;
    }
}
