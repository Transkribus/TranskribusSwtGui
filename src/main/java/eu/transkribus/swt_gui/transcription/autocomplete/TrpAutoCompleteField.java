package eu.transkribus.swt_gui.transcription.autocomplete;

import java.util.HashSet;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class TrpAutoCompleteField {
	
	private SimpleContentProposalProvider proposalProvider;
	private MyContentProposalAdapter adapter;
	
	String[] proposals=new String[]{};

	public TrpAutoCompleteField(Control control, IControlContentAdapter controlContentAdapter, String[] proposals,
			KeyStroke keyStroke, char[] autoActivationCharacters)
			{
		assert(proposals != null);
		this.proposals = proposals;
		proposalProvider = new SimpleContentProposalProvider(proposals);
		proposalProvider.setFiltering(true);
		adapter = new MyContentProposalAdapter(control, controlContentAdapter,
				proposalProvider, keyStroke, autoActivationCharacters);
		adapter.setPropagateKeys(false);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		
		adapter.setAutoActivateOnLetter(true);
		adapter.setAutoCloseOnSpace(true);
		adapter.setPopupSize(new Point(300, 150));	
	}
	
	public MyContentProposalAdapter getAdapter() { return adapter; }
	
	/**
	 * Set the Strings to be used as content proposals.
	 * 
	 * @param proposals
	 *            the array of Strings to be used as proposals.
	 */
	public void setProposals(String[] proposals) {
		assert(proposals != null);
		this.proposals = proposals;
		proposalProvider.setProposals(proposals);
	}
	
	public void addProposals(String... proposals) {
		HashSet<String> literals = new HashSet<String>();
		
		for (String p : getProposals()) {
			literals.add(p);
		}
		for (String p : proposals) {
			literals.add(p);
		}
		
		setProposals(literals.toArray(new String[literals.size()]));
	}
	
	public String[] getProposals() {
		return proposals;
	}

}
