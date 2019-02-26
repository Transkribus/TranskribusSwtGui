package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrGroundTruthContentProvider implements ITreeContentProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrGroundTruthContentProvider.class);
	List<TrpHtr> htrs;
	Storage store = Storage.getInstance();
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
		if (newInput instanceof List<?>) {
			this.htrs = ((List<TrpHtr>) newInput);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof List<?>) {
			return ((List<TrpHtr>) inputElement).toArray();			
		} else if (inputElement instanceof TrpHtr) {
			return getChildren((TrpHtr) inputElement);
		} else if (inputElement instanceof GroundTruthSet) {
			return getChildren((GroundTruthSet) inputElement);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TrpHtr) {
			return getChildren((TrpHtr) parentElement);
		} else if (parentElement instanceof GroundTruthSet) {
			return getChildren((GroundTruthSet) parentElement);
		}
		return null;
	}
	
	private Object[] getChildren(TrpHtr htr) {
		GroundTruthSet trainSet = null;
		GroundTruthSet valSet = null;
		if(htr.hasTrainGt()) {
			trainSet = new GroundTruthSet(htr.getHtrId(), GtSetType.TRAIN, htr.getNrOfTrainGtPages());
		}
		if(htr.hasValidationGt()) {
			valSet = new GroundTruthSet(htr.getHtrId(), GtSetType.VALIDATION, htr.getNrOfValidationGtPages());
		}
		if(trainSet != null && valSet != null) {
			return new GroundTruthSet[] { trainSet, valSet };
		} else if (trainSet != null) {
			return new GroundTruthSet[] { trainSet };
		}
		return null;
	}
	
	private Object[] getChildren(GroundTruthSet gt) {
		switch(gt.getSetType()) {
		case TRAIN:
			try {
				return store.getConnection().getHtrTrainData(store.getCollId(), gt.getHtrId()).toArray();
			} catch (SessionExpiredException | IllegalArgumentException e) {
				logger.error("Could not retrieve HTR train data set for HTR = " + gt.getHtrId(), e);
			}
		case VALIDATION:
			try {
				return store.getConnection().getHtrValidationData(store.getCollId(), gt.getHtrId()).toArray();
			} catch (SessionExpiredException | IllegalArgumentException e) {
				logger.error("Could not retrieve HTR validation data set for HTR = " + gt.getHtrId(), e);
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof List<?>) {
			return null;
		} else if (element instanceof TrpHtr) {
			return htrs;
		} else if (element instanceof GroundTruthSet) {
			final int htrId = ((GroundTruthSet) element).getHtrId();
			for(TrpHtr h : htrs) {
				if(h.getHtrId() == htrId) {
					return h;
				}
			}
		}

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof List<?> && ((List<?>) element).size() > 0) {
			return true;
		} else if (element instanceof TrpHtr) {
			return ((TrpHtr) element).hasTrainGt();
		} else if (element instanceof GroundTruthSet) {
			final GroundTruthSet s = ((GroundTruthSet) element);
			for(TrpHtr h : htrs) {
				if(h.getHtrId() == s.getHtrId()) {
					switch (s.getSetType()) {
					case TRAIN:
						return h.hasTrainGt();
					case VALIDATION:
						return h.hasValidationGt();
					}
				}
			}
		}
		return false;
	}
	
	static class GroundTruthSet {
		private final int htrId;
		private final int nrOfPages;
		private final GtSetType setType;
		public GroundTruthSet(int htrId, GtSetType setType, int nrOfPages) {
			this.htrId = htrId;
			this.setType = setType;
			this.nrOfPages = nrOfPages;
		}
		public int getHtrId() {
			return htrId;
		}
		public GtSetType getSetType() {
			return setType;
		}
		public int getNrOfPages() {
			return nrOfPages;
		}
	}
	
	static enum GtSetType {
		TRAIN,
		VALIDATION;
	}
}
