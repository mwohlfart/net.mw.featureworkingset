/**
 * 
 */
package net.mw.featureworkingset.internal.jdt;

import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.IBaseLabelProvider;

/**
 * @author Michael
 * 
 */
@SuppressWarnings("restriction")
public class JdtUtil {

	public static IBaseLabelProvider getTreeLabelProvider() {
		AppearanceAwareLabelProvider javaElementLabelProvider = new AppearanceAwareLabelProvider( //
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED, //
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS); //

		return new DecoratingJavaLabelProvider(javaElementLabelProvider);
	}

	public static IBaseLabelProvider getTableLabelProvider() {
		AppearanceAwareLabelProvider javaElementLabelProvider = new AppearanceAwareLabelProvider( //
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED | JavaElementLabels.ROOT_POST_QUALIFIED | JavaElementLabels.P_POST_QUALIFIED, //
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS);

		return new DecoratingJavaLabelProvider(javaElementLabelProvider);
	}

}
