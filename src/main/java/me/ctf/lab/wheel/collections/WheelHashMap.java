package me.ctf.lab.wheel.collections;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * 轮子hashMap
 *
 * @author: chentiefeng[chentiefeng@linzikg.com]
 * @create: 2019-12-24 13:55
 */
public class WheelHashMap<K, V> {

    public static final int DEFAULT_CAPACITY = 16;

    /**
     * 链表
     */
    static class Node<K, V> {
        int hash;
        K key;
        V val;
        Node<K, V> next;

        public Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("{" + key + ":" + val + "}");
            Node<K, V> n = next;
            while (n != null) {
                s.append("-->{").append(n.key).append(":").append(n.val).append("}");
                n = n.next;
            }
            return s.toString();
        }
    }

    /**
     * hash表
     */
    private Node<K, V>[] table;
    /**
     * 负载因子，默认0.75
     */
    private float loadFactor = 0.75f;
    /**
     * 阈值，当hash表的元素数量到达阈值时，扩容
     */
    private int threshold;
    /**
     * 元素数量
     */
    private int size = 0;

    /**
     * put方法
     *
     * @param key
     * @param val
     */
    public void put(K key, V val) {
        //1.初始化hash table，默认长度16
        if (table == null) {
            table = new Node[DEFAULT_CAPACITY];
            threshold = (int) (DEFAULT_CAPACITY * loadFactor);
        }
        //2.放进hash表，hash冲突链表走一波
        int hash = hash(key);
        //计算hash坐标，用位运算效率高，从这里可以看出数组长度为2的倍数优势
        int idx = hash & (table.length - 1);
        Node<K, V> c = table[idx];
        if (c == null) {
            //当前hash位置（哈希桶位置）没有元素，新增一个
            table[idx] = new Node<>(hash, key, val, null);
        } else {
            //hash冲突，链表走一波
            while (c.next != null) {
                if (c.hash == hash && (Objects.equals(key, c.key))) {
                    //链表中间有相同的key，直接替换key和val，跳出循环
                    c.key = key;
                    c.val = val;
                    break;
                }
            }
            if (c.next == null) {
                //链表中间跳出循环的next肯定不为null，所以进入这里的肯定是最后一个节点
                if (c.hash == hash && (Objects.equals(key, c.key))) {
                    //尝试匹配最后节点
                    c.key = key;
                    c.val = val;
                } else {
                    //直接在最后节点加上
                    c.next = new Node<>(hash, key, val, null);
                }
            }
        }
        //3.负载到了扩容一波
        size++;
        if (size > threshold) {
            //到负载扩容
            resize();
        }
        //每次都把table打印出来调试用
        StringJoiner s = new StringJoiner(", ");
        for (int i = 0; i < table.length; i++) {
            s.add(i + "=" + table[i]);
        }
        System.out.println("table:" + table.length + ", [" + s + "]");
    }

    private void resize() {
        //1.计算新数组大小
        //扩容为原来数组长度的2倍
        int oldLength = table.length;
        Node<K, V>[] oldTable = table;
        int newLength = table.length << 1;
        Node<K, V>[] newTable = new Node[newLength];
        table = newTable;
        //2.负载因子重新计算，在原来负载因子左移1，相当于乘以2，和newTable.length * loadFactor效果相同，但是更快
        threshold = threshold << 1;
        //3.把老数组rehash到新数组
        for (int i = 0; i < oldTable.length; i++) {
            Node<K, V> old = oldTable[i];
            if (old == null) {
                continue;
            }
            if (old.next == null) {
                //重新计算原来key的hash值对应的数组下标
                //非链表直接放到新hash表里面
                newTable[old.hash & (newLength - 1)] = old;
            } else {
                //有链表的需要逐条循环计算下标，比如原hash表长度16，5位置有个链表：5--->21--->37--->53---->null，扩容为32后，会形成2个链表，位置分表在5、21两个位置5--->37  21--->53
                //这里hashMao的设计非常漂亮，参考用
                Node<K, V> loHead = null, loTail = null;
                Node<K, V> hiHead = null, hiTail = null;
                Node<K, V> next;
                do {
                    next = old.next;
                    //length=16,old.hash=5: 0000 0101 & 0001 0000 = 0000 0000 = 0 最高位=0
                    //length=16,old.hash=21: 0001 0101 & 0001 0000 = 0001 0000 = 16 最高位!=0
                    //length=16,old.hash=37: 0010 0101 & 0001 0000 = 0000 0000 = 0 最高位=0
                    //length=16,old.hash=53: 0011 0101 & 0001 0000 = 0001 0000 = 16 最高位=!0
                    //一个数&2的倍数，要么等于2的倍数，要么等于0，根据这个规则拆分出来两个链表
                    if ((old.hash & oldLength) == 0) {
                        if (loTail == null) {
                            loHead = old;
                        } else {
                            loTail.next = old;
                        }
                        loTail = old;
                    } else {
                        if (hiTail == null) {
                            hiHead = old;
                        } else {
                            hiTail.next = old;
                        }
                        hiTail = old;
                    }
                } while ((old = next) != null);
                if (loTail != null) {
                    loTail.next = null;
                    newTable[i] = loHead;
                }
                if (hiTail != null) {
                    hiTail.next = null;
                    newTable[i + oldLength] = hiHead;
                }
            }
        }
    }

    /**
     * 复制hashMap的hash方法
     *
     * @param key
     * @return
     */
    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public V get(K key) {
        int hash = hash(key);
        Node<K, V> ret = table[hash & (table.length - 1)];
        if (ret == null) {
            return null;
        }
        do {
            if (ret.hash == hash && Objects.equals(ret.key, key)) {
                return ret.val;
            }
        } while ((ret = ret.next) != null);
        return null;
    }
}
