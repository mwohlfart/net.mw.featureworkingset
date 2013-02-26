package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 */

/**
 * @author Michael
 * 
 */
public class CreateFeatureWorkingSetHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;

			String name = "";
			List<IAdaptable> elements = new ArrayList<IAdaptable>();

			if (ssel.size() == 1) {
				IProject project = (IProject) ssel.getFirstElement();
				name = project.getName();
				elements.add(project);
				
				IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
				IWorkingSet workingSet = manager.createWorkingSet(name, elements.toArray(new IAdaptable[elements.size()]));
				workingSet.setId(FeatureWorkingSetPlugin.FEATUREWORKINGSET_ID);
				
				IWorkingSetEditWizard editWizard = manager.createWorkingSetEditWizard(workingSet);

				WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), editWizard);
				dialog.create();

				if (dialog.open() == Window.OK) {
					workingSet = editWizard.getSelection();
					manager.addWorkingSet(workingSet);
					// TODO: Workaround -> initiate a working set content update here since it is deactivated in updater when working set is added.
					workingSet.setName(name);
				}
			}

		}

		return null;
	}

}
