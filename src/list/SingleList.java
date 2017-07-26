package list;

public class SingleList<T> {
    Node head;

    public SingleList() {
    }

    public void add(T ele) {
        Node n = new Node(ele);
        if (head == null) {
            head = n;
        } else {
            Node p = head;
            while (p.next != null)
                p = p.next;
            p.next = n;
        }
    }

    public void delete() {
        if (head == null)
            return;
        if (head.next == null) {
            head = null;
            return;
        }
        Node p = head;
        Node q = p.next;
        while (q.next != null) {
            p = q;
            q = q.next;
        }
        p.setNext(null);
    }

    public void reverse() {
        if (head == null || head.next == null) {
            return;
        }
        Node p = head.next;
        Node q = p.next;
        Node t;
        while (q != null) {
            t = q.next;
            q.next = p;
            p = q;
            q = t;
        }
    }

    class Node {
        T element;
        Node next;

        Node() {
        }

        Node(T ele) {
            element = ele;
        }

        void setNext(Node n) {
            next = n;
        }
    }

}
