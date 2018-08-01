package eu.transkribus.swt_gui.canvas;

import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.core.util.Event;
import eu.transkribus.swt_gui.canvas.shapes.TableDimension;
import eu.transkribus.swt_gui.table_editor.BorderFlags;

public interface ICanvasContextMenuListener {
	
	default void handleDeleteItemEvent(DeleteItemEvent event) {}
	
	default void handleTableHelpEvent(TableHelpEvent event) {}

	default void handleTableBorderEditEvent(TableBorderEditEvent event) {}
	
	default void handleTableBorderDialogEvent(TableBorderDialogEvent event) {}

	default void handleDeleteTableEvent(DeleteTableEvent event) {}

	default void handleRemoveIntermediatePointsTableEvent(RemoveIntermediatePointsTableEvent event) {}

	default void handleSplitTableCellEvent(SplitTableCellEvent event) {}
	
	default void handleMergeTableCellsEvent(MergeTableCellsEvent event) {}

	default void handleSelectTableCellsEvent(SelectTableCellsEvent event) {}
	
	default void handleCreateDefaultLineEvent(CreateDefaultLineEvent event) {}
	
	default void handleFocusTableEvent(FocusTableEvent event) {}
	
	default void handleSetStructureEvent(SetStructureEvent event) {}
	
	default void handleEvent(Event event) {
		if (event instanceof DeleteItemEvent) {
			handleDeleteItemEvent((DeleteItemEvent) event);
		}
		else if (event instanceof CreateDefaultLineEvent) {
			handleCreateDefaultLineEvent((CreateDefaultLineEvent) event);
		}
		else if (event instanceof SelectTableCellsEvent) {
			handleSelectTableCellsEvent((SelectTableCellsEvent) event);
		} 
		else if (event instanceof SplitTableCellEvent) {
			handleSplitTableCellEvent((SplitTableCellEvent) event);
		}
		else if (event instanceof RemoveIntermediatePointsTableEvent) {
			handleRemoveIntermediatePointsTableEvent((RemoveIntermediatePointsTableEvent) event);
		}
		else if (event instanceof DeleteTableEvent) {
			handleDeleteTableEvent((DeleteTableEvent) event);
		}
		else if (event instanceof TableBorderEditEvent) {
			handleTableBorderEditEvent((TableBorderEditEvent) event);
		}
		else if (event instanceof TableHelpEvent) {
			handleTableHelpEvent((TableHelpEvent) event);
		}
		else if (event instanceof FocusTableEvent) {
			handleFocusTableEvent((FocusTableEvent) event);
		}
		else if (event instanceof MergeTableCellsEvent) {
			handleMergeTableCellsEvent((MergeTableCellsEvent) event);
		}
		else if (event instanceof SetStructureEvent) {
			handleSetStructureEvent((SetStructureEvent) event);
		}
		else if (event instanceof TableBorderDialogEvent) {
			handleTableBorderDialogEvent((TableBorderDialogEvent) event); 
		}
		
	}

	@SuppressWarnings("serial")
	public static class DeleteItemEvent extends Event {
		public DeleteItemEvent(Object source) {
			super(source, "DeleteItemEvent");
		}
	}
	
	@SuppressWarnings("serial")
	public static class CreateDefaultLineEvent extends Event {
		public CreateDefaultLineEvent(Object source) {
			super(source, "CreateDefaultLineEvent");
		}
	}
	
	@SuppressWarnings("serial")
	public static class SelectTableCellsEvent extends Event {
		public TableDimension dim = null;
		
		public SelectTableCellsEvent(Object source, TableDimension dim) {
			super(source);
			this.dim = dim;
		}
	}
	
	@SuppressWarnings("serial")
	public static class FocusTableEvent extends Event {
		
		public FocusTableEvent(Object source) {
			super(source);
		}
	}
	
	@SuppressWarnings("serial")
	public static class SplitTableCellEvent extends Event {		
		public SplitTableCellEvent(Object source) {
			super(source);
		}
	}
	
	@SuppressWarnings("serial")
	public static class MergeTableCellsEvent extends Event {
		public MergeTableCellsEvent(Object source) {
			super(source);
		}
	}
	
	@SuppressWarnings("serial")
	public static class RemoveIntermediatePointsTableEvent extends Event {		
		public RemoveIntermediatePointsTableEvent(Object source) {
			super(source);
		}
	}
	
	@SuppressWarnings("serial")
	public static class DeleteTableEvent extends Event {
		public TableDimension dim = null;
		
		public DeleteTableEvent(Object source, TableDimension dim) {
			super(source);
			this.dim = dim;
		}
	}
	
	@SuppressWarnings("serial")
	public static class TableBorderEditEvent extends Event {
		public BorderFlags borderFlags;
		public boolean set;
		
		public TableBorderEditEvent(Object source, BorderFlags borderFlags, boolean set) {
			super(source);
			this.borderFlags = borderFlags;
			this.set = set;
		}
	}
	
	@SuppressWarnings("serial")
	public static class TableBorderDialogEvent extends Event {
		public TableBorderDialogEvent(Object source) {
			super(source);
		}
	}
	
	@SuppressWarnings("serial")
	public static class TableHelpEvent extends Event {	
		public TableHelpEvent(Object source) {
			super(source);
		}
	}
	
	@SuppressWarnings("serial")
	public static class SetStructureEvent extends Event {
		public StructureTag st;
		
		public SetStructureEvent(Object source, StructureTag st) {
			super(source);
			this.st = st;
		}
	}
	
}
