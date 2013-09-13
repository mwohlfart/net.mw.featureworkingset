/**
 * 
 */
package net.mw.featureworkingset.internal.jdt;

import net.mw.featureworkingset.FeatureWorkingSetUtil.UnresolvedBundle;

import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

/**
 * @author Michael
 * 
 */
@SuppressWarnings("restriction")
public class JdtUtil {
	
	public static class MyLabelProvider implements ILabelProvider {
		
		private DecoratingJavaLabelProvider javaLabelProvider;

		public MyLabelProvider(DecoratingJavaLabelProvider javaLabelProvider) {
			this.javaLabelProvider = javaLabelProvider;
		}

		@Override
		public Image getImage(Object element) {
			return javaLabelProvider.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof UnresolvedBundle) {
				return ((UnresolvedBundle)element).toString();
			}
			return javaLabelProvider.getText(element);
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			javaLabelProvider.addListener(listener);
		}

		@Override
		public void dispose() {
			javaLabelProvider.dispose();
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return javaLabelProvider.isLabelProperty(element, property);
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			javaLabelProvider.removeListener(listener);
		}

	}
	

	public static IBaseLabelProvider getTreeLabelProvider() {
		AppearanceAwareLabelProvider javaElementLabelProvider = new AppearanceAwareLabelProvider( //
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED, //
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS); //

		return new MyLabelProvider(new DecoratingJavaLabelProvider(javaElementLabelProvider));
	}

	public static IBaseLabelProvider getTableLabelProvider() {
		AppearanceAwareLabelProvider javaElementLabelProvider = new AppearanceAwareLabelProvider( //
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED | JavaElementLabels.ROOT_POST_QUALIFIED | JavaElementLabels.P_POST_QUALIFIED, //
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS);

		return new MyLabelProvider(new DecoratingJavaLabelProvider(javaElementLabelProvider));
	}

}
