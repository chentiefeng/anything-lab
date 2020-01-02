package me.ctf.lab.datastructure;

/**
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2020-01-02 09:47
 */
public class BinarySearchTreeTest {
    public static void main(String[] args) {
        BinarySearchTree<Integer> bst = new BinarySearchTree<>();
        bst.add(6);
        bst.add(11);
        bst.add(2);
        bst.add(1);
        bst.add(8);
        bst.add(15);
        bst.add(9);
        bst.add(10);
        bst.remove(6);
        System.out.println(bst.toHtmlShowStr());
    }
}
