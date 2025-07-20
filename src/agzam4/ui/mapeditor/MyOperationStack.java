package agzam4.ui.mapeditor;

import arc.struct.Seq;

public class MyOperationStack {

    private static final int maxSize = 10;
    private Seq<MyDrawOperation> stack = new Seq<>();
    private int index = 0;

    public MyOperationStack() {}

    public void clear(){
        stack.clear();
        index = 0;
    }

    public void add(MyDrawOperation action){
        stack.truncate(stack.size + index);
        index = 0;
        stack.add(action);

        if(stack.size > maxSize){
            stack.remove(0);
        }
    }

    public boolean canUndo(){
        return !(stack.size - 1 + index < 0);
    }

    public boolean canRedo(){
        return !(index > -1 || stack.size + index < 0);
    }

    public void undo(){
        if(!canUndo()) return;

        stack.get(stack.size - 1 + index).undo();
        index--;
    }

    public void redo(){
        if(!canRedo()) return;

        index++;
        stack.get(stack.size - 1 + index).redo();

    }

	public Object size() {
		return stack.size + ":" + index;
	}

}
