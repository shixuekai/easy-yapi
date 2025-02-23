package com.itangcent.idea.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.itangcent.common.utils.cast
import com.itangcent.idea.swing.ActiveWindowProvider
import com.itangcent.intellij.context.ActionContext
import java.awt.*
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.TableColumn
import javax.swing.tree.*


object SwingUtils {

    fun focus(apiCallDialog: Dialog) {
        ActionContext.getContext()!!.runInSwingUI {
            apiCallDialog.requestFocus()
        }
    }

    fun expandOrCollapseNode(tree: JTree, expanded: Boolean) {
        val node = tree.model.root as DefaultMutableTreeNode
        expandOrCollapseNode(tree, node, expanded)
    }

    fun expandOrCollapseNode(tree: JTree, node: DefaultMutableTreeNode, expanded: Boolean) {

        for (treeNode in node.children()) {
            expandOrCollapseNode(tree, treeNode as DefaultMutableTreeNode, expanded)
        }

        if (!expanded && node.isRoot) {
            return
        }
        val path = TreePath(node.path)
        if (expanded) {
            tree.expandPath(path)
        } else {
            tree.collapsePath(path)
        }
    }

    fun underLine(component: JComponent) {
//        component.isOpaque = true
        component.border = BorderFactory.createMatteBorder(0, 0, 1, 0, component.foreground)
        component.background = component.parent.background
    }

    fun immersed(component: JComponent) {
//        component.isOpaque = true
        component.border = BorderFactory.createMatteBorder(0, 0, 0, 0, component.foreground)
        component.background = component.parent.background
    }

    fun centerWindow(component: Component) {
        val toolkit = Toolkit.getDefaultToolkit()
        val scmSize = toolkit.screenSize
        val width = component.width
        val height = component.height

        component.setLocation(
            scmSize.width / 2 - width / 2,
            scmSize.height / 2 - height / 2
        )
    }

    fun preferableWindow(): Window? {
        val context = ActionContext.getContext() ?: return null
        try {
            context.instance(ActiveWindowProvider::class).activeWindow().cast(Window::class)?.let { return it }
        } catch (e: com.google.inject.ConfigurationException) {
        }
        WindowManager.getInstance().suggestParentWindow(context.instance(Project::class))
            ?.let { return it }
        return null
    }
}

fun MouseEvent?.isDoubleClick(): Boolean {
    if (this == null || this.isConsumed) return false

    if (this.isPopupTrigger) return false
    if (this.clickCount == 1 && this.isControlDown) {
        return true
    } else if (this.clickCount == 2 && this.button == MouseEvent.BUTTON1) {
        return true
    }
    return false
}

fun JTable.findColumn(index: Int): TableColumn? {
    return this.getColumn(this.getColumnName(index))
}

fun TreeModel.reload() {
    (this as? DefaultTreeModel)?.reload()
}

fun TreeModel.reload(node: TreeNode) {
    (this as? DefaultTreeModel)?.reload(node)
}

fun TreeModel.clear(node: TreeNode) {
    if (node !is DefaultMutableTreeNode) return
    if (node.childCount > 0) {
        node.removeAllChildren()
        this.reload(node)
    }
}

fun TreeModel.clear() {
    (this.root as? TreeNode)?.let { this.clear(it) }
}

fun TreeModel.remove(node: TreeNode) {
    if (node !is DefaultMutableTreeNode) return
    node.removeFromParent()
    this.reload(node)
}

fun Component.initAfterShown(init: () -> Unit) {
    var notInit = true

    this.addComponentListener(object : ComponentListener {
        override fun componentResized(e: ComponentEvent?) {
        }

        override fun componentMoved(e: ComponentEvent?) {
        }

        override fun componentShown(e: ComponentEvent?) {
            synchronized(this) {
                if (notInit) {
                    notInit = false
                } else {
                    return
                }
            }
            init()
        }

        override fun componentHidden(e: ComponentEvent?) {
        }
    })
}

fun Component.onResized(handle: (ComponentEvent?) -> Unit) {
    this.addComponentListener(object : ComponentListener {
        override fun componentResized(e: ComponentEvent?) {
            handle(e)
        }

        override fun componentMoved(e: ComponentEvent?) {
            handle(e)
        }

        override fun componentShown(e: ComponentEvent?) {
            handle(e)
        }

        override fun componentHidden(e: ComponentEvent?) {
            handle(e)
        }
    })
}

fun Component.minusHeight(margin: Int, vararg components: Component): Int {
    var h = this.height
    for (component in components) {
        if (component.isVisible) {
            h -= component.height
            h -= margin
        }
    }
    return h
}

fun Component.bottomAlignTo(component: Component) {
    val h = component.location.y + component.height - this.location.y
    this.setSizeIfNecessary(this.width, h)
}

fun Component.setSizeIfNecessary(width: Int, height: Int) {
    if (this.width != width || this.height != height) {
        this.setSize(width, height)
        this.doLayout()
    }
}

fun Component.adjustWithIfNecessary(gap: Int) {
    setSizeIfNecessary(this.width + gap, this.height)
}

fun Container.visit(handle: (Component) -> Unit) {
    for (component in this.components) {
        handle(component)
        if (component is Container) {
            component.visit(handle)
        }
    }
}


@Suppress("UNCHECKED_CAST")
fun <T> JList<T>.getModelElements(): List<T> {
    val model = this.model
    if (model is List<*>) {
        return model as List<T>
    }
    val modelElements: ArrayList<T> = ArrayList(model.size)
    (0 until model.size).forEach { modelElements.add(model.getElementAt(it)) }
    return modelElements
}

fun <T> JList<T>.getMutableComboBoxModel(): MutableComboBoxModel<T>? {
    return this.model as? MutableComboBoxModel
}

fun <T> JList<T>.addElement(element: T) {
    getMutableComboBoxModel()?.addElement(element)
}

fun <T> JList<T>.removeElementAt(index: Int) {
    if (index > -1 && index < this.model.size) {
        getMutableComboBoxModel()?.removeElementAt(index)
    }
}