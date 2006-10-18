package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.core.util.FastStack;

import java.util.ArrayList;
import java.util.List;


/**
 * A generic {@link com.thoughtworks.xstream.io.HierarchicalStreamWriter} for DOM writer
 * implementations. The implementation manages a list of top level DOM nodes. Every time the
 * last node is closed on the node stack, the next started node is added to the list. This list
 * can be retrieved using the {@link DocumentWriter#getTopLevelNodes()} method.
 * 
 * @author Laurent Bihanic
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public abstract class AbstractDocumentWriter extends AbstractXmlWriter implements DocumentWriter {

    private final List result = new ArrayList();
    private final FastStack nodeStack = new FastStack(16);

    /**
     * Constructs an AbstractDocumentWriter.
     * 
     * @param container the top level container for the nodes to create (may be
     *            <code>null</code>)
     * @parame replacer the object that creates XML-friendly names
     * @since upcoming
     */
    public AbstractDocumentWriter(final Object container, final XmlFriendlyReplacer replacer) {
        super(replacer);
        if (container != null) {
            nodeStack.push(container);
            result.add(container);
        }
    }

    public final void startNode(final String name) {
        final Object node = createNode(name);
        nodeStack.push(node);
    }

    /**
     * Create a node. The provided node name is not yet XML friendly. If {@link #getCurrent()}
     * returns <code>null</code> the node is a top level node.
     * 
     * @param name the node name
     * @return the new node
     * @since upcoming
     */
    protected abstract Object createNode(String name);

    public final void endNode() {
        endNodeInternally();
        final Object node = nodeStack.pop();
        if (nodeStack.size() == 0) {
            result.add(node);
        }
    }

    /**
     * Called when a node ends. Hook for derived implementations.
     * 
     * @since upcoming
     */
    public void endNodeInternally() {
    }

    /**
     * @since upcoming
     */
    protected final Object getCurrent() {
        return nodeStack.peek();
    }

    public List getTopLevelNodes() {
        return result;
    }

    public void flush() {
        // don't need to do anything
    }

    public void close() {
        // don't need to do anything
    }
}
