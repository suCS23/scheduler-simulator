public class queue {

    private node<process> front;
    private node<process> rear;
    private int size;
    private String schType;

    queue(String schType) {
        this.schType = schType;
    }

    void enqueue(process p) {
        if (isEmpty()) {
            front = new node<>(p, null);
            rear = front;
        } else {
            rear.next = new node<>(p, rear.next);
            rear = rear.next;
        }
        size++;
    }

    node<process> dequeue() {
        if (!isEmpty()) {
            node<process> temp = front;
            front = front.next;
            return temp;
        }
        return null;
    }

    void explode() {while (!isEmpty()) {front = front.next;}}

    node<process> peek() { return (!isEmpty())? front : null;}

    boolean isEmpty() {return front == null;}

    int getSize() {return size;}
}

class node<process> {
    process p;
    node<process> next;

    public node(process p, node<process> next) {
        this.p = p;
        this.next = next;
    }
}
