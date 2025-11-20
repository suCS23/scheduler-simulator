public class queue {

    private node front;
    private node rear;
    private final String schType; 

    queue(String schType) {
        this.schType = schType;
    }

    void enqueue(process p) {
        node newNode = new node(p, null);
        
        if (schType.equals("rm")) {
            // Sort by Memory Required (Ascending)
            if (isEmpty() || front.p.getMr() > p.getMr()) {
                newNode.next = front;
                front = newNode;
                if (rear == null) rear = front;
            } else {
                node current = front;
                while (current.next != null && current.next.p.getMr() <= p.getMr()) {
                    current = current.next;
                }
                newNode.next = current.next;
                current.next = newNode;
                if (newNode.next == null) rear = newNode;
            }
        } else {
            // FIFO
            if (isEmpty()) {
                front = newNode;
                rear = front;
            } else {
                rear.next = newNode;
                rear = newNode;
            }
        }
    }

    node dequeue() {
        if (!isEmpty()) {
            node temp = front;
            front = front.next;
            if (front == null) rear = null;
            return temp;
        }
        return null;
    }

    node peek() { return (!isEmpty()) ? front : null; }
    boolean isEmpty() { return front == null; }
    node getFront() { return front; }

    public String getQueueContentString() {
        if (isEmpty()) return "  EMPTY\n";
        StringBuilder sb = new StringBuilder();
        node current = front;
        while (current != null) {
            sb.append(String.format("Job ID %d , %.2f Cycles left to completion.%n", 
                current.p.getPid(), (double)current.p.getRemainingTime()));
            current = current.next;
        }
        return sb.toString();
    }
}

class node {
    public process p;
    public node next;
    public node(process p, node next) { this.p = p; this.next = next; }
}