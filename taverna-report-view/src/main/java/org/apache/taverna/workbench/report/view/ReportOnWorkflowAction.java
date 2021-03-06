/**
 *
 */
package org.apache.taverna.workbench.report.view;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workbench.MainWindow;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.ui.SwingWorkerCompletionWaiter;
import org.apache.taverna.workbench.ui.Workbench;
import org.apache.taverna.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ReportOnWorkflowAction extends AbstractAction {

	private final boolean includeTimeConsuming;
	private final boolean remember;
	private Dataflow specifiedDataflow;
	private static final String namedComponent = "reportView";

	private final FileManager fileManager;
	private final ReportManager reportManager;
	private final Workbench workbench;
	private final EditManager editManager;

	public ReportOnWorkflowAction(String name, boolean includeTimeConsuming, boolean remember,
			EditManager editManager, FileManager fileManager, ReportManager reportManager, Workbench workbench) {
		super(name);
		this.includeTimeConsuming = includeTimeConsuming;
		this.remember = remember;
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		this.workbench = workbench;
		this.specifiedDataflow = null;
	}

	public ReportOnWorkflowAction(String name, Dataflow dataflow, boolean includeTimeConsuming,
			boolean remember, EditManager editManager, FileManager fileManager, ReportManager reportManager,
			Workbench workbench) {
		super(name);
		this.specifiedDataflow = dataflow;
		this.includeTimeConsuming = includeTimeConsuming;
		this.remember = remember;
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		this.workbench = workbench;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (validateWorkflow()) {
			checkStatus();
		}
	}

	/**
	 * Check the status and pop up a warning if something is wrong.
	 *
	 */
	public void checkStatus() {
		Dataflow dataflow;
		if (specifiedDataflow == null) {
			dataflow = fileManager.getCurrentDataflow();
		} else {
			dataflow = specifiedDataflow;
		}
		Status status = reportManager.getStatus(dataflow);
		int messageType;
		String message;
		if (status.equals(Status.OK)) {
			messageType = JOptionPane.INFORMATION_MESSAGE;
			message = "Workflow validated OK.";

		} else {
			StringBuffer sb = new StringBuffer();
			Map<Object, Set<VisitReport>> reports = reportManager.getReports(dataflow);
			int errorCount = 0;
			int warningCount = 0;
			// Find warnings
			for (Entry<Object, Set<VisitReport>> entry : reports.entrySet()) {
				for (VisitReport report : entry.getValue()) {
					if (report.getStatus().equals(Status.SEVERE)) {
						errorCount++;
					} else if (report.getStatus().equals(Status.WARNING)) {
						warningCount++;
					}
				}
			}
			if (status.equals(Status.WARNING)) {
				messageType = JOptionPane.WARNING_MESSAGE;
				message = "Validation reported ";
			} else { // SEVERE
				messageType = JOptionPane.ERROR_MESSAGE;
				message = "Validation reported ";
				if (errorCount == 1) {
					message += "one error";
				} else {
					message += errorCount + " errors";
				}
				if (warningCount != 0) {
					message += " and ";
				}
			}
			if (warningCount == 1) {
				message += "one warning";
			} else if (warningCount > 0) {
				message += warningCount + " warnings";
			}
		}
		JOptionPane.showMessageDialog(MainWindow.getMainWindow(), message, "Workflow validation",
				messageType);
		workbench.getPerspectives().setWorkflowPerspective();
		workbench.makeNamedComponentVisible(namedComponent);
	}

	/**
	 * Perform validation on workflow.
	 *
	 * @return <code>true</code> if the validation was not cancelled.
	 */
	public boolean validateWorkflow() {
		Dataflow dataflow;
		if (specifiedDataflow == null) {
			dataflow = fileManager.getCurrentDataflow();
		} else {
			dataflow = specifiedDataflow;
		}
		ValidateSwingWorker validateSwingWorker = new ValidateSwingWorker(dataflow,
				includeTimeConsuming, remember, editManager, reportManager);
		ValidateInProgressDialog dialog = new ValidateInProgressDialog();
		validateSwingWorker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
		validateSwingWorker.execute();

		// Give a chance to the SwingWorker to finish so we do not have to
		// display
		// the dialog if copying of the workflow is quick (so it won't flicker
		// on the screen)
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {

		}
		if (!validateSwingWorker.isDone()) {
			dialog.setVisible(true); // this will block the GUI
		}
		boolean userCancelled = dialog.hasUserCancelled(); // see if user
		// cancelled the
		// dialog

		if (userCancelled) {
			validateSwingWorker.cancel(true);
		}
		return !userCancelled;

	}

}
