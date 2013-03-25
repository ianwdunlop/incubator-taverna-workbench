/**
 *
 */
package net.sf.taverna.t2.workbench.retry;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workflow.edits.ConfigureEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;

/**
 * @author alanrw
 *
 */
public class RetryConfigureAction extends AbstractAction {

	private Frame owner;
	private final DispatchStackLayer retryLayer;
	private final RetryContextualView retryContextualView;

	private EditManager editManager;
	private FileManager fileManager;

	private static Logger logger = Logger.getLogger(RetryConfigureAction.class);

	public RetryConfigureAction(Frame owner, RetryContextualView retryContextualView,
			DispatchStackLayer retryLayer, EditManager editManager, FileManager fileManager) {
		super("Configure");
		this.owner = owner;
		this.retryContextualView = retryContextualView;
		this.retryLayer = retryLayer;
		this.editManager = editManager;
		this.fileManager = fileManager;
	}

	public void actionPerformed(ActionEvent e) {
		String title = "Retries for service " + retryLayer.getProcessor().getLocalName();
		final JDialog dialog = new HelpEnabledDialog(owner, title, true);
		RetryConfigurationPanel retryConfigurationPanel = new RetryConfigurationPanel(
				retryLayer.getConfiguration());
		dialog.add(retryConfigurationPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton okButton = new JButton(new OKAction(dialog, retryConfigurationPanel));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(retryConfigurationPanel));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(dialog));
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public class ResetAction extends AbstractAction {

		private final RetryConfigurationPanel retryConfigurationPanel;

		public ResetAction(RetryConfigurationPanel retryConfigurationPanel) {
			super("Reset");
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			retryConfigurationPanel.populate();
		}

	}

	public class OKAction extends AbstractAction {

		private final RetryConfigurationPanel retryConfigurationPanel;
		private final JDialog dialog;

		public OKAction(JDialog dialog, RetryConfigurationPanel retryConfigurationPanel) {
			super("OK");
			this.dialog = dialog;
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			if (retryConfigurationPanel.validateConfig()) {
				try {
					Edit edit = new ConfigureEdit(retryLayer, retryConfigurationPanel.getConfiguration());
					editManager.doDataflowEdit(fileManager.getCurrentDataflow(), edit);
					dialog.setVisible(false);
					if (retryContextualView != null) {
						retryContextualView.refreshView();
					}
				} catch (EditException e1) {
					logger.warn("Could not configure retries", e1);
					JOptionPane.showMessageDialog(owner, "Could not configure retries",
							"An error occured when configuring retries: " + e1.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}

	}

	public class CancelAction extends AbstractAction {

		private final JDialog dialog;

		public CancelAction(JDialog dialog) {
			super("Cancel");
			this.dialog = dialog;

		}

		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
		}

	}

}
