package net.mw.featureworkingset;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;

interface IFeatureWorkingSet {
	
	public IWorkingSet getWorkingSet();
	
	String getWorkingSetName();

	public IProject getFeatureProject();
	
	public IProject[] getChildren();
	
	public boolean contains(IProject project);
}