package org.apache.taverna.workbench.views.results;
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

import java.awt.CardLayout;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowRunSelectionEvent;
import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.workbench.views.results.saveactions.SaveAllResultsSPI;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import org.apache.taverna.platform.report.ActivityReport;
import org.apache.taverna.platform.report.ProcessorReport;
import org.apache.taverna.platform.report.WorkflowReport;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.port.Port;

/**
 * Component for displaying the input and output values of workflow and processor invocations.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ResultsComponent extends JPanel implements Updatable {
	private static final Logger logger = Logger.getLogger(ResultsComponent.class);

	private final RunService runService;
	private final SelectionManager selectionManager;
	private final RendererRegistry rendererRegistry;
	private final List<SaveAllResultsSPI> saveAllResultsSPIs;
	private final List<SaveIndividualResultSPI> saveIndividualResultSPIs;

	private CardLayout cardLayout = new CardLayout();
	private Updatable updatableComponent;
	private Map<String, ReportView> workflowResults = new HashMap<>();
	private Map<String, Map<Processor, ReportView>> processorResults = new HashMap<>();
	private SelectionManagerObserver selectionManagerObserver = new SelectionManagerObserver();
	private String workflowRun;

	public ResultsComponent(RunService runService, SelectionManager selectionManager,
			RendererRegistry rendererRegistry, List<SaveAllResultsSPI> saveAllResultsSPIs,
			List<SaveIndividualResultSPI> saveIndividualResultSPIs) {
		this.runService = runService;
		this.selectionManager = selectionManager;
		this.rendererRegistry = rendererRegistry;
		this.saveAllResultsSPIs = saveAllResultsSPIs;
		this.saveIndividualResultSPIs = saveIndividualResultSPIs;

		setLayout(cardLayout);

		selectionManager.addObserver(selectionManagerObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionManager.removeObserver(selectionManagerObserver);
	}

	@Override
	public void update() {
		if (updatableComponent != null)
			updatableComponent.update();
	}

	public void setWorkflowRun(String workflowRun) throws InvalidRunIdException {
		if (workflowRun == null)
			return;
		this.workflowRun = workflowRun;

		DataflowSelectionModel selectionModel = selectionManager
				.getWorkflowRunSelectionModel(workflowRun);
		Set<Object> selectionSet = selectionModel.getSelection();
		if (selectionSet.size() == 1) {
			Object selection = selectionSet.iterator().next();
			if (selection instanceof Processor) {
				showProcessorResults((Processor) selection);
				return;
			}
		}

		showWorkflowResults();
	}

	public void addWorkflowRun(String workflowRun) throws InvalidRunIdException {
		WorkflowReport workflowReport = runService.getWorkflowReport(workflowRun);
		ReportView reportView = new ReportView(workflowReport, rendererRegistry,
				saveAllResultsSPIs, saveIndividualResultSPIs);
		add(reportView, workflowRun);
		workflowResults.put(workflowRun, reportView);
		DataflowSelectionModel selectionModel = selectionManager
				.getWorkflowRunSelectionModel(workflowRun);
		selectionModel.addObserver(new DataflowSelectionObserver());
	}

	public void removeWorkflowRun(String workflowRun) {
		ReportView removedWorkflowResults = workflowResults.remove(workflowRun);
		if (removedWorkflowResults != null)
			remove(removedWorkflowResults);
		Map<Processor, ReportView> removedProcessorResults = processorResults.remove(workflowRun);
		if (removedProcessorResults != null)
			for (ReportView reportView: removedProcessorResults.values())
				remove(reportView);
	}

	private void showWorkflowResults() throws InvalidRunIdException {
		if (!workflowResults.containsKey(workflowRun))
			addWorkflowRun(workflowRun);
		updatableComponent = workflowResults.get(workflowRun);
		cardLayout.show(this, workflowRun);
		update();
	}

	private void showProcessorResults(Processor processor) throws InvalidRunIdException {
		if (!processorResults.containsKey(workflowRun))
			processorResults.put(workflowRun, new HashMap<Processor, ReportView>());
		Map<Processor, ReportView> components = processorResults.get(workflowRun);
		if (!components.containsKey(processor)) {
			WorkflowReport workflowReport = runService.getWorkflowReport(workflowRun);
			ProcessorReport processorReport = findProcessorReport(workflowReport, processor);
			ReportView reportView = new ReportView(processorReport, rendererRegistry,
					saveAllResultsSPIs, saveIndividualResultSPIs);
			components.put(processor, reportView);
			add(reportView, String.valueOf(reportView.hashCode()));
		}
		updatableComponent = components.get(processor);
		cardLayout.show(this, String.valueOf(updatableComponent.hashCode()));
		update();
	}

	private ProcessorReport findProcessorReport(WorkflowReport workflowReport,
			Processor processor) {
		URI workflowIdentifier = workflowReport.getSubject().getIdentifier();
		if (processor.getParent().getIdentifier().equals(workflowIdentifier)) {
			for (ProcessorReport processorReport : workflowReport
					.getProcessorReports())
				if (processorReport.getSubject().getName()
						.equals(processor.getName()))
					return processorReport;
			return null;
		}

		for (ProcessorReport processorReport : workflowReport
				.getProcessorReports())
			for (ActivityReport activityReport : processorReport
					.getActivityReports()) {
				WorkflowReport nestedWorkflowReport = activityReport
						.getNestedWorkflowReport();
				if (nestedWorkflowReport != null) {
					ProcessorReport report = findProcessorReport(
							nestedWorkflowReport, processor);
					if (report != null)
						return report;
				}
			}
		return null;
	}

	private class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			try {
				if (message instanceof WorkflowRunSelectionEvent)
					setWorkflowRun(((WorkflowRunSelectionEvent) message)
							.getSelectedWorkflowRun());
			} catch (InvalidRunIdException e) {
				logger.warn("Invalid workflow run", e);
			}
		}
	}

	private final class DataflowSelectionObserver implements Observer<DataflowSelectionMessage> {
		@Override
		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			if (message.getType() != DataflowSelectionMessage.Type.ADDED)
				return;

			Object element = message.getElement();
			if (element instanceof Processor) {
				showProcessorResults((Processor) element);
				return;
			}

			showWorkflowResults();

			if (element instanceof Port) {
				Port port = (Port) element;
				if (updatableComponent instanceof ReportView) {
					ReportView reportView = (ReportView) updatableComponent;
					reportView.selectPort(port);
				}
			}
		}
	}
}
