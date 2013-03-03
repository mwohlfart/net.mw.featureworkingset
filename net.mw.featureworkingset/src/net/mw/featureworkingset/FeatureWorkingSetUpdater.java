/*******************************************************************************
 * This is an adapted version of JavaWorkingSetUpdater.java originally contributed by IBM
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Wohlfart - Added support for Feature Working Sets
 *******************************************************************************/
package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.PlatformUI;


public class FeatureWorkingSetUpdater implements IWorkingSetUpdater, IElementChangedListener {

	private interface IFeatureWorkingSet {
		
		public IWorkingSet getWorkingSet();
		
		String getWorkingSetName();

		public IProject getFeatureProject();
		
		public IProject[] getChildren();
	}
	
	private List<IFeatureWorkingSet> fWorkingSets;
	
	private static IFeatureWorkingSet createFeatureWorkingSet(final IWorkingSet workingSet) {
		
		IFeatureWorkingSet result = new IFeatureWorkingSet() {
			
			@Override
			public IWorkingSet getWorkingSet() {
				return workingSet;
			}
			
			@Override
			public String getWorkingSetName() {
				return workingSet.getName();
			}
			
			@Override
			public IProject getFeatureProject() {
				return FeatureWorkingSetUtil.getFeatureProject(workingSet);
			}
			
			@Override
			public IProject[] getChildren() {
				IProject featureProject = getFeatureProject();
				return FeatureWorkingSetUtil.getFeatureWorkingsetContents(featureProject);
			}
		};
		
		return result;
	}

	private static void updateFeatureWorkingSet(IFeatureWorkingSet featureWorkingSet) {
		IProject[] children = featureWorkingSet.getChildren();
		featureWorkingSet.getWorkingSet().setElements(children);
	}

	private static boolean isFeatureFileChange(IResource resource, int kind) {
		return kind == IResourceDelta.CHANGED && (resource instanceof IFile) && ((IFile)resource).getName().equals("feature.xml"); //$NON-NLS-1$
	}

	private static boolean isProjectOpenStateChange(IResource resource, int kind, int flags) {
		return resource.getType() == IResource.PROJECT
			&& kind == IResourceDelta.CHANGED
			&& (flags & IResourceDelta.OPEN) != 0;
	}

	public FeatureWorkingSetUpdater() {
		fWorkingSets= new ArrayList<IFeatureWorkingSet>();
		JavaCore.addElementChangedListener(this);
		
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		
		workingSetManager.addPropertyChangeListener(new IPropertyChangeListener() {
			
			private boolean alreadyUpdating = false;

			@Override
			public synchronized void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				
					if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
						
						IWorkingSet workingSet = (IWorkingSet) event.getNewValue();
						
						if (FeatureWorkingSetPlugin.FEATUREWORKINGSET_ID.equals(workingSet.getId())) {
							if (alreadyUpdating) {
								alreadyUpdating = false;
							} else {
								alreadyUpdating = true;
								IFeatureWorkingSet featureWorkingSet = internalGetFeatureWorkingSet(workingSet);
								if (featureWorkingSet != null) {
									updateFeatureWorkingSet(featureWorkingSet);
								}
							}
						}
					}
					
					if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
						IWorkingSet workingSet = (IWorkingSet) event.getNewValue();
						
						if (FeatureWorkingSetPlugin.FEATUREWORKINGSET_ID.equals(workingSet.getId())) {
							IFeatureWorkingSet featureWorkingSet = internalGetFeatureWorkingSet(workingSet);
							updateFeatureWorkingSet(featureWorkingSet);
						}
					}
				}
				
				
				
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(IWorkingSet workingSet) {
		synchronized (fWorkingSets) {
			IFeatureWorkingSet featureWorkingSet = createFeatureWorkingSet(workingSet);
			fWorkingSets.add(featureWorkingSet);
			
			// TODO Workaround! working sets are not available in WorkingSetModel if they do not contain elements -> do not update here but where the working set is created.  
//			updateFeatureWorkingSet(featureWorkingSet);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean remove(IWorkingSet workingSet) {
		synchronized(fWorkingSets) {
			IFeatureWorkingSet featureWorkingSet = internalGetFeatureWorkingSet(workingSet);
			if (featureWorkingSet != null) {
				fWorkingSets.remove(featureWorkingSet);
			}
		}
		return true;
	}

	public boolean contains(IWorkingSet workingSet) {
		synchronized(fWorkingSets) {
			return internalContainsFeatureWorkingSet(workingSet);
		}
	}

	public void dispose() {
		synchronized(fWorkingSets) {
			fWorkingSets.clear();
		}
		JavaCore.removeElementChangedListener(this);
	}

	public void elementChanged(ElementChangedEvent event) {
		IResourceDelta[] resourceDeltas = event.getDelta().getResourceDeltas();
		
		if (resourceDeltas != null) {
			for (IResourceDelta resourceDelta : resourceDeltas) {
				processResourceDelta(resourceDelta);
			}
		}
		
	}

	private void processResourceDelta(IResourceDelta resourceDelta) {
		IResource resource= resourceDelta.getResource();
		int kind= resourceDelta.getKind();
		int flags = resourceDelta.getFlags();
		
		if (isProjectOpenStateChange(resource, kind, flags)) {
			IProject changedProject = resource.getProject();
			
			IFeatureWorkingSet featureWorkingSet = internalGetFeatureWorkingSet(changedProject);
			if (featureWorkingSet != null) {
				updateFeatureWorkingSet(featureWorkingSet);
			}
		}
		
		if (isFeatureFileChange(resource, kind)) {
			IProject changedProject = resource.getProject();
			
			for (IFeatureWorkingSet workingSet : fWorkingSets) {
				if (changedProject.equals(workingSet.getFeatureProject())) {
					updateFeatureWorkingSet(workingSet);
				}
			}
		}
		
		IResourceDelta[] children= resourceDelta.getAffectedChildren();
		for (int i= 0; i < children.length; i++) {
			processResourceDelta(children[i]);
		}
		
	}

	private IFeatureWorkingSet internalGetFeatureWorkingSet(IProject featureProject) {
		for (IFeatureWorkingSet featureWorkingSet : fWorkingSets) {
			if (featureWorkingSet.getFeatureProject().equals(featureProject)) {
				return featureWorkingSet;
			}
		}
		return null;
	}

	private IFeatureWorkingSet internalGetFeatureWorkingSet(IWorkingSet workingSet) {
		for (IFeatureWorkingSet featureWorkingSet : fWorkingSets) {
			if (featureWorkingSet.getWorkingSetName().equals(workingSet.getName())) {
				return featureWorkingSet;
			}
		}
		
		return null;
	}

	private boolean internalContainsFeatureWorkingSet(IWorkingSet workingSet) {
		return internalGetFeatureWorkingSet(workingSet) != null;
	}
}
