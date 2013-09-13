/*******************************************************************************
 * This is an adapted version of JavaWorkingSetUpdater.java originally contributed by IBM
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Wohlfart - Added support for Feature Working Sets
 *******************************************************************************/
package net.mw.featureworkingset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.PlatformUI;


public class FeatureWorkingSetUpdater implements IWorkingSetUpdater {
	
	private static class WorkingSetUtil {
		
		private static boolean isNameChange(String property) {
			return property.equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE);
		}

		private static boolean isContentChange(String property) {
			return property.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE);
		}
		
		private static boolean isFeatureWorkingSet(IWorkingSet workingSet) {
			return FeatureWorkingSetPlugin.FEATUREWORKINGSET_ID.equals(workingSet.getId());
		}
		
	}

	private List<IFeatureWorkingSet> fWorkingSets;
	private JavaElementChangedListener javaElementChangedListener;
	private ResourceChangeListener resourceChangeListener;
	
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
		
		javaElementChangedListener = new JavaElementChangedListener();
		JavaCore.addElementChangedListener(javaElementChangedListener);
		
		resourceChangeListener = new ResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
		
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		
		workingSetManager.addPropertyChangeListener(new IPropertyChangeListener() {
			
			private boolean alreadyUpdating = false;

			@Override
			public synchronized void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				
					if (WorkingSetUtil.isContentChange(property)) {
						IWorkingSet workingSet = (IWorkingSet) event.getNewValue();
						
						if (WorkingSetUtil.isFeatureWorkingSet(workingSet)) {
							if (alreadyUpdating) {
								alreadyUpdating = false;
							} else {
								alreadyUpdating = true;
								IFeatureWorkingSet featureWorkingSet = internalGetFeatureWorkingSet(workingSet);
								if (featureWorkingSet != null) {
									FeatureWorkingSetUtil.updateFeatureWorkingSet(featureWorkingSet);
								}
							}
						}
					}
					
					if (WorkingSetUtil.isNameChange(property)) {
						IWorkingSet workingSet = (IWorkingSet) event.getNewValue();
						
						if (WorkingSetUtil.isFeatureWorkingSet(workingSet)) {
							IFeatureWorkingSet featureWorkingSet = internalGetFeatureWorkingSet(workingSet);
							if (featureWorkingSet != null) {
								FeatureWorkingSetUtil.updateFeatureWorkingSet(featureWorkingSet);
							}
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
			IFeatureWorkingSet featureWorkingSet = FeatureWorkingSetUtil.createFeatureWorkingSet(workingSet);
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
		JavaCore.removeElementChangedListener(javaElementChangedListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
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
	
	private class ResourceChangeListener implements IResourceChangeListener {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
//			System.out.println("Resource changed ("+event.toString()+"");
			
//			processResourceDeltaRecursive(event.getDelta());
		}

		private void processResourceDeltaRecursive(IResourceDelta delta) {
			processResourceDelta(delta);
			
			for (IResourceDelta childDelta : delta.getAffectedChildren()) {
				processResourceDelta(childDelta);
			}
			
		}

		private void processResourceDelta(IResourceDelta delta) {
			if (delta.getResource() instanceof IProject) {
				
				IProject project = (IProject) delta.getResource();
				
				for (IFeatureWorkingSet fws : fWorkingSets) {
					if (fws.contains(project)) {
						try {
							project.close(new NullProgressMonitor());
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			}
			
		}
		
	}
	
	private class JavaElementChangedListener implements IElementChangedListener {

		@Override
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
					FeatureWorkingSetUtil.updateFeatureWorkingSet(featureWorkingSet);
				}
			}
			
			if (isFeatureFileChange(resource, kind)) {
				IProject changedProject = resource.getProject();
				
				for (IFeatureWorkingSet workingSet : fWorkingSets) {
					if (changedProject.equals(workingSet.getFeatureProject())) {
						FeatureWorkingSetUtil.updateFeatureWorkingSet(workingSet);
					}
				}
			}
			
			IResourceDelta[] children= resourceDelta.getAffectedChildren();
			for (int i= 0; i < children.length; i++) {
				processResourceDelta(children[i]);
			}
			
		}
		
	}
}
