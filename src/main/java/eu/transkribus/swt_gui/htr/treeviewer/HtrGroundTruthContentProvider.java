package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.GroundTruthSelectionDescriptor.GtSetType;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.ACollectionBoundStructuredContentProvider;

public class HtrGroundTruthContentProvider extends ACollectionBoundStructuredContentProvider implements ITreeContentProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrGroundTruthContentProvider.class);
	List<TrpHtr> htrs;
	
	public HtrGroundTruthContentProvider(final Integer colId) {
		super(colId);
	}
	
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
		} else if (inputElement instanceof HtrGtDataSet) {
			return getChildren((HtrGtDataSet) inputElement);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TrpHtr) {
			return getChildren((TrpHtr) parentElement);
		} else if (parentElement instanceof HtrGtDataSet) {
			return getChildren((HtrGtDataSet) parentElement);
		}
		return null;
	}
	
	HtrGtDataSet[] getChildren(TrpHtr htr) {
		HtrGtDataSet trainSet = null;
		HtrGtDataSet valSet = null;
		if(htr.hasTrainGt()) {
			trainSet = new HtrGtDataSet(htr, GtSetType.TRAIN);
		}
		if(htr.hasValidationGt()) {
			valSet = new HtrGtDataSet(htr, GtSetType.VALIDATION);
		}
		if(trainSet != null && valSet != null) {
			return new HtrGtDataSet[] { trainSet, valSet };
		} else if (trainSet != null) {
			return new HtrGtDataSet[] { trainSet };
		}
		return null;
	}
	
	HtrGtDataSetElement[] getChildren(HtrGtDataSet gt) {
		List<TrpGroundTruthPage> gtList = null;
		switch(gt.getSetType()) {
		case TRAIN:
			try {
				gtList = store.getConnection().getHtrTrainData(super.getCollId(), gt.getHtrId());
			} catch (SessionExpiredException | IllegalArgumentException e) {
				logger.error("Could not retrieve HTR train data set for HTR = " + gt.getHtrId(), e);
			}
			break;
		case VALIDATION:
			try {
				gtList = store.getConnection().getHtrValidationData(super.getCollId(), gt.getHtrId());
			} catch (SessionExpiredException | IllegalArgumentException e) {
				logger.error("Could not retrieve HTR validation data set for HTR = " + gt.getHtrId(), e);
			}
			break;
		}
		if(gtList == null) {
			return null;
		} else {
			List<HtrGtDataSetElement> children = gtList.stream()
					.map(g -> new HtrGtDataSetElement(gt, g))
					.collect(Collectors.toList());
			return children.toArray(new HtrGtDataSetElement[children.size()]);
		}
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof List<?>) {
			return null;
		} else if (element instanceof TrpHtr) {
			return htrs;
		} else if (element instanceof HtrGtDataSet) {
			return((HtrGtDataSet) element).getHtr();
		} else if (element instanceof HtrGtDataSetElement) {
			return ((HtrGtDataSetElement) element).getParentHtrGtDataSet();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof List<?> && ((List<?>) element).size() > 0) {
			return true;
		} else if (element instanceof TrpHtr) {
			return ((TrpHtr) element).hasTrainGt();
		} else if (element instanceof HtrGtDataSet) {
			final HtrGtDataSet s = ((HtrGtDataSet) element);
			TrpHtr h = ((HtrGtDataSet) element).getHtr();	
			switch (s.getSetType()) {
			case TRAIN:
				return h.hasTrainGt();
			case VALIDATION:
				return h.hasValidationGt();
			}
		}
		return false;
	}
	
	/**
	 * An instance of this type represents an HTR GroundTruth data set (e.g. train or validation set) and is used for 
	 * displaying the ground truth set level in a HTR treeviewer.
	 */
	public static class HtrGtDataSet implements Comparable<HtrGtDataSet> {
		private final TrpHtr htr;
		private final GtSetType setType;
		public HtrGtDataSet(TrpHtr htr, GtSetType setType) {
			this.htr = htr;
			this.setType = setType;
		}
		public int getHtrId() {
			return htr.getHtrId();
		}
		public TrpHtr getHtr() {
			return htr;
		}
		public GtSetType getSetType() {
			return setType;
		}
		public int getNrOfPages() {
			switch(setType) {
			case TRAIN:
				return htr.getNrOfTrainGtPages();
			case VALIDATION:
				return htr.getNrOfValidationGtPages();
			}
			return -1;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((htr == null) ? 0 : htr.hashCode());
			result = prime * result + ((setType == null) ? 0 : setType.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HtrGtDataSet other = (HtrGtDataSet) obj;
			if (htr == null) {
				if (other.htr != null)
					return false;
			} else if (htr.getHtrId() != other.htr.getHtrId())
				return false;
			if (setType != other.setType)
				return false;
			return true;
		}
		@Override
		public int compareTo(HtrGtDataSet o) {
			if (this.getHtrId() > o.getHtrId()) {
				return 1;
			}
			if (this.getHtrId() < o.getHtrId()) {
				return -1;
			}
			if (GtSetType.TRAIN.equals(this.getSetType()) 
					&& GtSetType.VALIDATION.equals(o.getSetType())) {
				return 1;
			}
			if (GtSetType.VALIDATION.equals(this.getSetType()) 
					&& GtSetType.TRAIN.equals(o.getSetType())) {
				return -1;
			}
			return 0;			
		}
	}
	
	/**
	 * TrpDocMetadata type that decorates a HtrGtDataSet. This type is needed to determine the origin of a GT doc that is loaded in Storage. 
	 */
	public static class TrpHtrGtDocMetadata extends TrpDocMetadata {
		private static final long serialVersionUID = -3302933027729222456L;
		private final HtrGtDataSet dataSet;
		public TrpHtrGtDocMetadata(HtrGtDataSet dataSet) {
			this.dataSet = dataSet;
			this.setTitle("HTR '" + dataSet.getHtr().getName() + "' " + dataSet.setType.getLabel());
		}
		public HtrGtDataSet getDataSet() {
			return dataSet;
		}
		@Override
		public Integer getStatus() {
			return TrpDocMetadata.STATUS_GROUND_TRUTH_DOC;
		}
	}
	
	/**
	 * An instance of this type represents an HTR GroundTruth data set element (e.g. a page from train or validation set) and is used for 
	 * displaying the ground truth page level in a HTR treeviewer. It wraps a TrpGroundTruthPage and has a reference to its parent HtrGtDataSet.
	 * <br><br>
	 * Using plain TrpGroundTruthPage objects would not allow to determine the original parent as a GroundTruthPage may be linked in 
	 * several HTRs (with different pageNr though). This is a problem when a single page is added to the selection and {@link HtrGroundTruthContentProvider#getParent(Object)} is called.
	 */
	public static class HtrGtDataSetElement {
		private final HtrGtDataSet parentHtrGtDataSet;
		private final TrpGroundTruthPage gtPage;
		public HtrGtDataSetElement(HtrGtDataSet parentHtrGtDataSet, TrpGroundTruthPage gtPage) {
			this.parentHtrGtDataSet = parentHtrGtDataSet;
			this.gtPage = gtPage;
		}
		public HtrGtDataSet getParentHtrGtDataSet() {
			return parentHtrGtDataSet;
		}
		public TrpGroundTruthPage getGroundTruthPage() {
			return gtPage;
		}
	}
}
