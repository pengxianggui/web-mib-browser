const ATTR_COMPONENT_FN = (nodeData) => {
    const MAP = {
        "Select": {
            component: 'fast-select',
            props: {
                ...nodeData.component.props,
                multiple: false,
            }
        },
        "Input": {
            component: 'el-input',
            props: {
                ...nodeData.component.props
            }
        },
        "InputNumber": {
            component: 'el-input-number',
            props: {
                ...nodeData.component.props
            }
        },
    }
    return MAP[nodeData.component.type]
}

const TABLE_COLUMN_COMPONENT_FN = (nodeData) => {
    const MAP = {
        "Select": {
            name: nodeData.name,
            component: 'fast-table-column-select',
            props: {
                ...nodeData.component.props,
                multiple: false,
            },
            editable: true
        },
        "Input": {
            name: nodeData.name,
            component: 'fast-table-column-input',
            props: {
                ...nodeData.component.props
            },
            editable: true
        },
        "InputNumber": {
            name: nodeData.name,
            component: 'fast-table-column-number',
            props: {
                ...nodeData.component.props
            },
            editable: true
        },
    }
    const componentType = nodeData.component.type
    if (Object.keys(MAP).indexOf(componentType) < 0) {
        return {
            name: nodeData.name,
            component: 'fast-table-column',
            editable: false
        }
    }

    const meta = MAP[componentType]
    if (!nodeData.canWrite) {
        meta.editable = false
    }
    return meta;
}

export function getTableIndexColumn() {
    return {
        name: 'index',
        component: 'fast-table-column',
        props: {
            label: '序号',
            width: 120
        }
    }
}

export function getTableColumnComponent(nodeData) {
    return TABLE_COLUMN_COMPONENT_FN(nodeData)
}

/**
 * 获取组件元数据。
 * @param nodeData 节点数据
 */
export function getAttrOrFieldComponent(nodeData) {
    return ATTR_COMPONENT_FN(nodeData)
}