package main.compression;

import java.util.*;
import java.io.ByteArrayOutputStream; // Optional

/**
 * Samuel Richard
 *  
 * Huffman instances provide reusable Huffman Encoding Maps for
 * compressing and decompressing text corpi with comparable
 * distributions of characters.
 */
public class Huffman {
    
    // -----------------------------------------------
    // Construction
    // -----------------------------------------------

    private HuffNode trieRoot;
    // TreeMap chosen here just to make debugging easier
    private TreeMap<Character, String> encodingMap;
    // Character that represents the end of a compressed transmission
    private static final char ETB_CHAR = 23;
    
    /**
     * Creates the Huffman Trie and Encoding Map using the character
     * distributions in the given text corpus
     * 
     * @param corpus A String representing a message / document corpus
     *        with distributions over characters that are implicitly used
     *        throughout the methods that follow. Note: this corpus ONLY
     *        establishes the Encoding Map; later compressed corpi may
     *        differ.
     */
    public Huffman (String corpus) {
        encodingMap = new TreeMap<>();
        PriorityQueue<HuffNode> leaves = new PriorityQueue<HuffNode>();
        String graveyard = "";
        int j;
        
        HuffNode ETB = new HuffNode(ETB_CHAR, 1);
        leaves.add(ETB);
        
      //Find character frequencies and create leaves
        for (int i = 0; i < corpus.length(); i++) {
            
            if (!graveyard.contains(Character.toString(corpus.charAt(i)))) {

                graveyard = graveyard + corpus.charAt(i);
                HuffNode newLeaf = new HuffNode(corpus.charAt(i), 1);
                leaves.add(newLeaf);
                
                j = i+1;
                while (j < corpus.length()) {
                    if (corpus.charAt(i) == corpus.charAt(j)) {
                        newLeaf.count++;
                    }
                    
                    j++;
                }
                
                
            }
        }
        
        
        //Construction of the Huffman Trie
        while (leaves.size() > 1) {
            HuffNode node1 = leaves.poll();
            HuffNode node2 = leaves.poll();
            HuffNode parent = new HuffNode('\0', node1.count + node2.count);
            parent.zeroChild = node1;
            parent.oneChild = node2;
            leaves.add(parent);
        }
        trieRoot = leaves.poll();

        //Construct Encoding Map
        int x = 0;
        while (x < corpus.length() + 1) {
            HuffNode node = trieRoot;
            String bitstring = "";


            while (!node.isLeaf()) {
                if (!node.zeroChild.visited) {
                    bitstring += "0";
                    node = node.zeroChild;
                } else {
                    bitstring += "1";
                    node.visited = true;
                    node = node.oneChild; 
                } 
            }

            node.visited = true;
            encodingMap.put(node.character, bitstring);
            x++;
        }
                
    }
    
    
    // -----------------------------------------------
    // Compression
    // -----------------------------------------------
    
    /**
     * Compresses the given String message / text corpus into its Huffman coded
     * bitstring, as represented by an array of bytes. Uses the encodingMap
     * field generated during construction for this purpose.
     * 
     * @param message String representing the corpus to compress.
     * @return {@code byte[]} representing the compressed corpus with the
     *         Huffman coded bytecode. Formatted as:
     *         (1) the bitstring containing the message itself, (2) possible
     *         0-padding on the final byte.
     */
    public byte[] compress (String message) {
        ByteArrayOutputStream byteSolution = new ByteArrayOutputStream();
        String stringSolution = "";
        int padding = 0;

        //Add bitString to stringSolution
        for (int i = 0; i < message.length(); i++) {
            stringSolution += encodingMap.get(message.charAt(i));
        }
        stringSolution += encodingMap.get(ETB_CHAR);
        
        //Add padding
        padding = 8 - stringSolution.length() % 8;
        if (stringSolution.length() % 8 != 0) {
            for (int i = 0; i < padding; i++) {
                stringSolution += "0";
            }
        }

        //Convert to bytes and add to array
        int bytes = stringSolution.length() / 8;
        for (int i = 0; i < bytes; i++) {
            byteSolution.write((byte) Integer.parseInt(stringSolution.substring(0, 8), 2));
            if (stringSolution.length() >= 8) {
                stringSolution = stringSolution.substring(8);
            }
        }
        
        return byteSolution.toByteArray();
    }
    
    
    // -----------------------------------------------
    // Decompression
    // -----------------------------------------------
    
    /**
     * Decompresses the given compressed array of bytes into their original,
     * String representation. Uses the trieRoot field (the Huffman Trie) that
     * generated the compressed message during decoding.
     * 
     * @param compressedMsg {@code byte[]} representing the compressed corpus with the
     *        Huffman coded bytecode. Formatted as:
     *        (1) the bitstring containing the message itself, (2) possible
     *        0-padding on the final byte.
     * @return Decompressed String representation of the compressed bytecode message.
     */
    public String decompress (byte[] compressedMsg) {

        String bitString = "";
        String solution = "";
        String tempString;

        //Convert to bitstring
        for (int i = 0; i < compressedMsg.length; i++) {
            tempString = Integer.toBinaryString(compressedMsg[i] & 0xff);
            if (tempString.length() < 8) {
                tempString = "0" + tempString;
            }
            bitString += tempString;
        }

        //Decompress
        HuffNode node = trieRoot;
        for (int i = 0; i < bitString.length(); i++) {
            
            if (bitString.charAt(i) == '0') {
                node = node.zeroChild;
            } else {
                node = node.oneChild;
            }

            if (node.isLeaf()) {
                if (node.character == ETB_CHAR) {
                    break;
                }
                solution += node.character;
                node = trieRoot;
            }
            
        }
        

        return solution;
    }
    
    
    // -----------------------------------------------
    // Huffman Trie
    // -----------------------------------------------
    
    /**
     * Huffman Trie Node class used in construction of the Huffman Trie.
     * Each node is a binary (having at most a left (0) and right (1) child), contains
     * a character field that it represents, and a count field that holds the 
     * number of times the node's character (or those in its subtrees) appear 
     * in the corpus.
     */
    private static class HuffNode implements Comparable<HuffNode> {
        
        HuffNode zeroChild, oneChild;
        char character;
        int count;
        boolean visited;
        
        HuffNode (char character, int count) {
            this.count = count;
            this.character = character;
            this.visited = false;
        }
        
        public boolean isLeaf () {
            return this.zeroChild == null && this.oneChild == null;
        }
        
        public int compareTo (HuffNode other) {
            if (this.count != other.count) {
                return this.count - other.count;
            }
            
            return this.character - other.character;
        }
        
    }

}
