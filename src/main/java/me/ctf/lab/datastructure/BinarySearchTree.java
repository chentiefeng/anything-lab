package me.ctf.lab.datastructure;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 二叉搜索树（二叉排序树）
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-31 16:04
 */
public class BinarySearchTree<T extends Comparable<T>> {
    /**
     * 节点
     *
     * @param <T>
     * @see Comparable
     */
    public static class Node<T extends Comparable<T>> {
        T data;
        Node<T> parent;
        Node<T> left;
        Node<T> right;

        public Node(T data, Node<T> parent, Node<T> left, Node<T> right) {
            this.data = data;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
    }

    public enum TraversalTypeEnum {
        /**
         * 前序
         */
        PRE,
        /**
         * 中序
         */
        IN,
        /**
         * 后序
         */
        POST

    }

    private Node<T> root;

    /**
     * 插入
     *
     * @param data
     * @return
     */
    public Node<T> add(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Can't add null.");
        }
        Node<T> c = root;
        if (c == null) {
            c = new Node<>(data, null, null, null);
            return (root = c);
        }
        return add(c, data, (p,t)->);
    }

    /**
     * 插入
     *
     * @param c
     * @param data
     * @return
     */
    private Node<T> add(Node<T> c, T data, BiFunction<Node<T>, T, Node<T>> leftFunction, BiFunction<Node<T>, T, Node<T>> rightFunction) {
        int compare = c.data.compareTo(data);
        if (compare == 0) {
            return c;
        }
        //c.data > data
        if (compare > 0) {
            if (c.left == null) {
                return (c.left = leftFunction.apply(c, data));
            }
            return add(c.left, data, leftFunction, rightFunction);
        }
        if (c.right == null) {
//            return (c.right = new Node<>(data, c, null, null));
            return (c.right = rightFunction.apply(c, data));
        }
        return add(c.right, data, leftFunction, rightFunction);
    }

    /**
     * 默认中序遍历
     *
     * @param consumer
     */
    public void forEach(Consumer<T> consumer) {
        if (root == null) {
            return;
        }
        Node<T> c = root;
        traversal(c, data -> {
            consumer.accept(data);
            return true;
        }, TraversalTypeEnum.IN);
    }

    /**
     * 可选遍历方式
     *
     * @param traversalTypeEnum
     * @param consumer
     * @see TraversalTypeEnum
     */
    public void forEach(TraversalTypeEnum traversalTypeEnum, Consumer<T> consumer) {
        traversal(root, data -> {
            consumer.accept(data);
            return true;
        }, traversalTypeEnum);
    }

    /**
     * 遍历，默认中序
     *
     * @param node
     * @param predicate break if return false
     */
    public static <T extends Comparable<T>> void traversal(Node<T> node, Predicate<T> predicate) {
        traversal(node, predicate, TraversalTypeEnum.IN);
    }

    /**
     * 遍历，可选类型
     *
     * @param node
     * @param predicate         break if return false
     * @param traversalTypeEnum
     * @see TraversalTypeEnum
     */
    public static <T extends Comparable<T>> void traversal(Node<T> node, Predicate<T> predicate, TraversalTypeEnum traversalTypeEnum) {
        if (node == null) {
            return;
        }
        if (TraversalTypeEnum.PRE.equals(traversalTypeEnum)) {
            if (!predicate.test(node.data)) {
                return;
            }
        }
        if (node.left != null) {
            traversal(node.left, predicate, traversalTypeEnum);
        }
        if (TraversalTypeEnum.IN.equals(traversalTypeEnum)) {
            if (!predicate.test(node.data)) {
                return;
            }
        }
        if (node.right != null) {
            traversal(node.right, predicate, traversalTypeEnum);
        }
        if (TraversalTypeEnum.POST.equals(traversalTypeEnum)) {
            predicate.test(node.data);
        }
    }

    /**
     * 根据数据寻找节点
     *
     * @param data
     * @return
     */
    public Node<T> node(T data) {
        return node(root, data);
    }

    /**
     * node
     *
     * @param c
     * @param data
     * @return
     */
    private Node<T> node(Node<T> c, T data) {
        int compare = c.data.compareTo(data);
        if (compare == 0) {
            return c;
        }
        //c.data > data
        if (compare > 0) {
            if (c.left == null) {
                return null;
            }
            return node(c.left, data);
        }
        if (c.right == null) {
            return null;
        }
        return node(c.right, data);
    }

    public static void main(String[] args) {
        BinarySearchTree<Integer> bst = new BinarySearchTree<>();
        bst.add(50);
        bst.add(30);
        bst.add(20);
        bst.add(40);
        bst.add(70);
        bst.add(60);
        bst.add(80);
        bst.forEach(TraversalTypeEnum.POST, System.out::println);
    }
}
