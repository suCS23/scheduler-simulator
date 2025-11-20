public class queue {

    // ==============================
    //          Attributes
    // ==============================
    private node front;
    private node rear;
    private final String schType; 
    private int size;

    // ==============================
    //     Constructor & Methods
    // ==============================
    queue(String schType) {
        this.schType = schType;
    }

    void enqueue(process p) {
        node newNode = new node(p, null);
        
        //sort according to memory requirment ascendingly 
        //else sort like a normal queue, fifo
        if (schType.equals("rm")) {

            if (isEmpty() || front.p.getMr() > p.getMr()) {
                newNode.next = front;
                front = newNode;

                if (rear == null) 
                    rear = front;
            } else {
                node pointer = front;

                while (pointer.next != null && pointer.next.p.getMr() <= p.getMr()) 
                    pointer = pointer.next;
                
                newNode.next = pointer.next;
                pointer.next = newNode;

                if (newNode.next == null) 
                    rear = newNode;
            }
        } else {
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
        size--;
        if (!isEmpty()) {
            node temp = front;
            front = front.next;

            if (front == null) 
                rear = null;
            
            return temp;
        }
        return null;
    }

    node peek() { return (!isEmpty()) ? front : null; }

    int getSize() { return size; }

    boolean isEmpty() { return front == null; }

    // ==============================
    //            Display
    // ==============================
    public String getQueueContentString() {
        if (isEmpty()) return "  EMPTY\n";

        StringBuilder sb = new StringBuilder();

        node pointer = front;
        while (pointer != null) {
            sb.append(String.format("Job ID %d , %.2f Cycles left to completion.%n", pointer.p.getPid(), (double)pointer.p.getRemainingTime()));
            pointer = pointer.next;
        }
        return sb.toString();
    }
}

class node {
    public process p;
    public node next;
    public node(process p, node next) { this.p = p; this.next = next; }
}