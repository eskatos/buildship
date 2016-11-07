/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.task;

import com.google.common.base.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Styled label provider for the task name column in the TaskView.
 */
public final class TaskNameLabelProvider extends LabelProvider implements IStyledLabelProvider {

    // We use internal, standalone icons because the platform's image overlay mechanism provides blurry images on high-resolution monitors

    @Override
    public String getText(Object element) {
        return element == null ? "" : getStyledText(element).getString();
    }

    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof ProjectTaskNode) {
            return getProjectTaskText((ProjectTaskNode) element);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorText((TaskSelectorNode) element);
        } else if (element instanceof ProjectNode) {
            return getProjectText((ProjectNode) element);
        } else if (element instanceof TaskGroupNode) {
            return getGroupText((TaskGroupNode) element);
        } else if (element instanceof FaultyProjectNode) {
            return new StyledString(((FaultyProjectNode)element).getProject().getName());
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }


    @Override
    public Image getImage(Object element) {
        if (element instanceof ProjectTaskNode) {
            return getProjectTaskImage((ProjectTaskNode) element);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorImage((TaskSelectorNode) element);
        } else if (element instanceof ProjectNode) {
            return getProjectImage((ProjectNode) element);
        } else if (element instanceof FaultyProjectNode) {
            return getFaultyProjectImage((FaultyProjectNode) element);
        } else if (element instanceof TaskGroupNode) {
            return getGroupImage((TaskGroupNode) element);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }

    private StyledString getTaskSelectorText(TaskSelectorNode taskSelector) {
        return new StyledString(taskSelector.getTaskSelector().getName());
    }

    private StyledString getProjectTaskText(ProjectTaskNode projectTask) {
        return new StyledString(projectTask.getProjectTask().getName());
    }

    private StyledString getGroupText(TaskGroupNode group) {
        return new StyledString(group.getName());
    }

    private StyledString getProjectText(ProjectNode project) {
        String name;
        Optional<IProject> workspaceProject = project.getWorkspaceProject();
        if (workspaceProject.isPresent()) {
            name = workspaceProject.get().getName();
        } else {
            name = project.getEclipseProject().getName();
        }
        return new StyledString(name);
    }

    private Image getProjectImage(ProjectNode project) {
        Optional<IProject> workspaceProject = project.getWorkspaceProject();
        if (!workspaceProject.isPresent() || !workspaceProject.get().isOpen()) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
        }

        if (isJavaProject(workspaceProject.get())) {
            return PluginImages.JAVA_PROJECT.withState(ImageState.ENABLED).getImage();
        } else {
            return PluginImages.PROJECT.withState(ImageState.ENABLED).getImage();
        }
    }

    private static boolean isJavaProject(IProject project) {
        try {
            return project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            return false;
        }
    }

    private Image getFaultyProjectImage(FaultyProjectNode node) {
        return PluginImages.FAULTY_PROJECT.withState(ImageState.ENABLED).getImage();
    }

    private Image getGroupImage(TaskGroupNode element) {
        return PluginImages.TASK_GROUP.withState(ImageState.ENABLED).getImage();
    }

    private Image getProjectTaskImage(ProjectTaskNode projectTask) {
        PluginImage image = projectTask.isPublic() ? PluginImages.PROJECT_TASK : PluginImages.PRIVATE_PROJECT_TASK;
        return image.withState(getImageState(projectTask)).getImage();
    }

    private Image getTaskSelectorImage(TaskSelectorNode taskSelector) {
        PluginImage image = taskSelector.isPublic() ? PluginImages.TASK : PluginImages.PRIVATE_TASK;
        return image.withState(getImageState(taskSelector)).getImage();
    }

    private ImageState getImageState(TaskNode taskNode) {
        return taskNode.getParentProjectNode().isIncludedProject() ? ImageState.DISABLED : ImageState.ENABLED;
    }
}
