package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowActivityIcon;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;

/**
 * An action for adding a nested workflow.
 *
 * @author Stian Soiland-Reyes
 *
 */
public class AddNestedWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = -2242979457902699028L;
	private final EditManager editManager;
	private final FileManager fileManager;
	private final MenuManager menuManager;

	public AddNestedWorkflowAction(EditManager editManager, FileManager fileManager, MenuManager menuManager) {
		super("Add nested workflow", DataflowActivityIcon.getDataflowIcon());
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;

	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		ImportWorkflowWizard wizard = new ImportWorkflowWizard(Utils.getParentFrame(parentComponent), editManager, fileManager, menuManager);
		wizard.setMergeEnabled(false);
		wizard.setVisible(true);
	}

}