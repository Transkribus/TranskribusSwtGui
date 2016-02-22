package eu.transkribus.swt_gui.transcription.autocomplete;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;

/**
 * This interface is used to listen to additional notifications from a
 * {@link ContentProposalAdapter}.
 * 
 * @since 3.3
 */
public interface MyIContentProposalListener2 /*extends IContentProposalListener2*/ {
	/**
	 * A content proposal popup has been opened for content proposal assistance.
	 * 
	 * @param adapter
	 *            the ContentProposalAdapter which is providing content proposal
	 *            behavior to a control
	 */
	public void proposalPopupOpened(MyContentProposalAdapter adapter);

	/**
	 * A content proposal popup has been closed.
	 * 
	 * @param adapter
	 *            the ContentProposalAdapter which is providing content proposal
	 *            behavior to a control
	 */
	public void proposalPopupClosed(MyContentProposalAdapter adapter);
}

