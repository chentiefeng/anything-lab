package me.ctf.lab.datastructure;

import com.google.gson.GsonBuilder;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        private T data;
        private Node<T> parent;
        private Node<T> left;
        private Node<T> right;
        private int depth;
        //序号
        private int index;

        private Node(T data, Node<T> parent, Node<T> left, Node<T> right, int depth) {
            this.data = data;
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.depth = depth;
        }

        public T getData() {
            return this.data;
        }

        public Node<T> getParent() {
            return this.parent;
        }

        public Node<T> getLeft() {
            return this.left;
        }

        private Node<T> getRight() {
            return this.right;
        }

        public int getDepth() {
            return this.depth;
        }


        @Override
        public String toString() {
            return this.data.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof Node) {
                return this.data.equals(((Node<T>) obj).data);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.data.hashCode();
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

    /**
     * 根节点
     */
    private Node<T> root;
    /**
     * 节点数量
     */
    private int size = 0;
    /**
     * 节点层级
     */
    private Set<Node<T>>[] nodeLevel;

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
            c = new Node<>(data, null, null, null, 1);
            size++;
            nodeLevel = (Set<Node<T>>[]) new Set[1];
            Set<Node<T>> set = new HashSet<>(1);
            set.add(c);
            nodeLevel[0] = set;
            return (root = c);
        }
        return addVal(c, data);
    }

    /**
     * add
     *
     * @param c
     * @param data
     * @return
     */
    private Node<T> addVal(Node<T> c, T data) {
        int compare = c.data.compareTo(data);
        if (compare == 0) {
            return c;
        }
        // c.data > data
        int depth = c.depth + 1;
        Node<T> ret;
        if (compare > 0) {
            if (c.left == null) {
                size++;
                ret = (c.left = new Node<>(data, c, null, null, depth));
                createNodeLevel(depth, ret);
                return ret;
            }
            return addVal(c.left, data);
        }
        if (c.right == null) {
            size++;
            ret = (c.right = new Node<>(data, c, null, null, depth));
            createNodeLevel(depth, ret);
            return ret;
        }
        return addVal(c.right, data);
    }

    private void createNodeLevel(int depth, Node<T> ret) {
        if (nodeLevel.length < depth) {
            Set<Node<T>>[] level = new Set[depth];
            System.arraycopy(nodeLevel, 0, level, 0, nodeLevel.length);
            nodeLevel = level;
        }
        Set<Node<T>> set = nodeLevel[depth - 1];
        if (set == null) {
            set = new HashSet<>();
            set.add(ret);
            nodeLevel[depth - 1] = set;
        } else {
            set.add(ret);
        }
    }

    /**
     * 匹配
     *
     * @param c
     * @param data
     * @return
     */
    private Node<T> match(Node<T> c, T data) {
        int compare = c.data.compareTo(data);
        if (compare == 0) {
            return c;
        }
        // c.data > data
        if (compare > 0) {
            if (c.left == null) {
                return null;
            }
            return match(c.left, data);
        }
        if (c.right == null) {
            return null;
        }
        return match(c.right, data);
    }

    /**
     * 深度优先便利默认中序遍历
     *
     * @param predicate
     */
    public void dfForEach(Predicate<T> predicate) {
        if (root == null) {
            return;
        }
        Node<T> c = root;
        deepFirstTraversal(c, predicate, TraversalTypeEnum.IN);
    }

    /**
     * 深度优先遍历，可选遍历方式
     *
     * @param traversalTypeEnum
     * @param predicate
     * @see TraversalTypeEnum
     */
    public void dfForEach(TraversalTypeEnum traversalTypeEnum, Predicate<T> predicate) {
        deepFirstTraversal(root, predicate, traversalTypeEnum);
    }

    /**
     * 广度优先遍历
     *
     * @param predicate break if return false
     */
    public void bfForEach(Predicate<T> predicate) {
        breadthFirstTraversal(root, predicate);
    }

    public int depth() {
        return this.nodeLevel.length;
    }

    public int size() {
        return this.size;
    }

    /**
     * 广度优先遍历
     *
     * @param n
     * @param predicate break if return false
     */
    public static <T extends Comparable<T>> void breadthFirstTraversal(Node<T> n, Predicate<T> predicate) {
        breadthFirstTraversalForNode(n, tNode -> predicate.test(tNode.data));
    }

    /**
     * 广度优先遍历
     *
     * @param n
     * @param predicate break if return false
     */
    public static <T extends Comparable<T>> void breadthFirstTraversalForNode(Node<T> n, Predicate<Node<T>> predicate) {
        Deque<Node<T>> queue = new ArrayDeque<>();
        queue.offer(n);
        Node<T> c;
        while (!queue.isEmpty()) {
            c = queue.poll();
            if (c.left != null) {
                queue.offer(c.left);
            }
            if (c.right != null) {
                queue.offer(c.right);
            }
            if (!predicate.test(c)) {
                break;
            }
        }
    }

    /**
     * 深度优先遍历，默认中序
     *
     * @param node
     * @param predicate break if return false
     */
    public static <T extends Comparable<T>> void deepFirstTraversal(Node<T> node, Predicate<T> predicate) {
        deepFirstTraversal(node, predicate, TraversalTypeEnum.IN);
    }

    /**
     * 深度优先遍历，可选类型
     *
     * @param node
     * @param predicate         break if return false
     * @param traversalTypeEnum
     * @see TraversalTypeEnum
     */
    public static <T extends Comparable<T>> void deepFirstTraversal(Node<T> node, Predicate<T> predicate,
                                                                    TraversalTypeEnum traversalTypeEnum) {
        deepFirstTraversalForNode(node, tNode -> predicate.test(tNode.data), traversalTypeEnum);
    }

    /**
     * 深度优先遍历，可选类型
     *
     * @param node
     * @param predicate         break if return false
     * @param traversalTypeEnum
     * @see TraversalTypeEnum
     */
    public static <T extends Comparable<T>> void deepFirstTraversalForNode(Node<T> node, Predicate<Node<T>> predicate,
                                                                           TraversalTypeEnum traversalTypeEnum) {
        if (node == null) {
            return;
        }
        if (TraversalTypeEnum.PRE.equals(traversalTypeEnum)) {
            if (!predicate.test(node)) {
                return;
            }
        }
        if (node.left != null) {
            deepFirstTraversalForNode(node.left, predicate, traversalTypeEnum);
        }
        if (TraversalTypeEnum.IN.equals(traversalTypeEnum)) {
            if (!predicate.test(node)) {
                return;
            }
        }
        if (node.right != null) {
            deepFirstTraversalForNode(node.right, predicate, traversalTypeEnum);
        }
        if (TraversalTypeEnum.POST.equals(traversalTypeEnum)) {
            predicate.test(node);
        }
    }

    /**
     * 根据数据寻找节点
     *
     * @param data
     * @return
     */
    public Node<T> node(T data) {
        return match(root, data);
    }

    /**
     * 根据数据删除节点
     *
     * @param data
     * @return
     */
    public void remove(T data) {
        remove(root, data);

    }

    /**
     * 从n开始，删除数据为data的节点
     *
     * @param n
     * @param data
     */
    private void remove(Node<T> n, T data) {
        Node<T> node = match(n, data);
        if (node == null) {
            return;
        }
        // 叶子节点/左右节点只有一个的节点
        if (node.left == null || node.right == null) {
            Node<T> pNode;
            Node<T> next = node.left == null ? node.right : node.left;
            if ((pNode = node.parent) == null) {
                // 根节点
                next.parent = null;
                root = next;
                adjustNodeLevel(node);
                node = null;
                size--;
                return;
            }
            int compare = node.data.compareTo(pNode.data);
            adjustNodeLevel(node);
            node = null;
            size--;
            if (compare > 0) {
                // right node
                pNode.right = next;
            } else {
                // left node
                pNode.left = next;
            }
            if (next != null) {
                next.parent = pNode;
            }
            return;
        }
        // 查找后继节点
        Node<T> next = node.right;
        while (next.left != null) {
            next = next.left;
        }
        // 后继节点替换当前节点
        node.data = next.data;
        // 删除后继节点
        remove(node.right, next.data);
    }

    /**
     * 调整节点层级
     *
     * @param node
     */
    private void adjustNodeLevel(Node<T> node) {
        breadthFirstTraversalForNode(node, n -> {
            int depth = n.depth;
            Set<Node<T>> set = nodeLevel[n.depth - 1];
            set.remove(n);
            n.depth--;
            if (n.left != null) {
                set.add(n.left);
            }
            if (n.right != null) {
                set.add(n.right);
            }
            if (set.isEmpty()) {
                int oldLen = nodeLevel.length;
                Set<Node<T>>[] newArray = new Set[oldLen - 1];
                System.arraycopy(nodeLevel, 0, newArray, 0, depth - 1);
                System.arraycopy(nodeLevel, depth, newArray, depth - 1, oldLen - depth);
                nodeLevel = newArray;
            }
            return true;
        });
    }

    /**
     * 根据深度查询节点
     *
     * @param depth
     * @return
     */
    public Set<Node<T>> nodesByDepth(int depth) {
        if (depth < 1 || depth > depth()) {
            throw new IllegalArgumentException("depth must in range [1, depth()]");
        }
        return this.nodeLevel[depth - 1];
    }

    public String toHtmlShowStr() {
        String[] arr = new String[(int) Math.pow(2, depth()) - 1];
        for (Set<Node<T>> nodes : nodeLevel) {
            for (Node<T> node : nodes) {
                if (node.parent == null) {
                    node.index = 0;
                } else {
                    if (node.data.compareTo(node.parent.data) > 0) {
                        node.index = node.parent.index * 2 + 2;
                    } else {
                        node.index = node.parent.index * 2 + 1;
                    }
                }
                arr[node.index] = node.toString();
            }
        }
        return "[" + String.join(",", arr) + "]";
    }

    public String toJsonString() {
        if (root == null) {
            return "{}";
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("id", root.toString());
        if (nodeLevel.length > 1) {
            List<Map<String, Object>> children = new ArrayList<>();
            children.add(map);
            List<Map<String, Object>> list;
            for (int i = 1; i < nodeLevel.length; i++) {
                list = new ArrayList<>();
                for (Map<String, Object> child : children) {
                    List<Map<String, Object>> collect = nodeLevel[i].stream()
                            .filter(n -> n.parent.toString().equals(child.get("id")))
                            .sorted(Comparator.comparing(o -> o.data))
                            .map(n -> {
                                Map<String, Object> m = new HashMap<>(2);
                                m.put("id", n.toString());
                                return m;
                            })
                            .collect(Collectors.toList());
                    if (collect.size() > 0) {
                        child.put("children", collect);
                        list.addAll(collect);
                    }
                }
                children = list;
            }
        }
        return new GsonBuilder().create().toJson(map);
    }

}
