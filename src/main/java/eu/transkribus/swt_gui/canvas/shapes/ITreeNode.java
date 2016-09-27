package eu.transkribus.swt_gui.canvas.shapes;

import java.util.List;

public interface ITreeNode<T> {
    T getParent();
    boolean hasParent();
    List<T> getChildren(boolean recursive);
    ICanvasShape getChild(ICanvasShape s);
    boolean hasChild(ICanvasShape s);
    T getChild(int i);
    int getNChildren();

//    void removeFromTree();
    
    void setParentAndAddAsChild(T parent);
    void setParent(T parent);
    void addChild(T child);
    void removeChildren();
    void removeFromParent();
    
    void setChildren(List<T> children);
    boolean removeChild(ICanvasShape s);    
}