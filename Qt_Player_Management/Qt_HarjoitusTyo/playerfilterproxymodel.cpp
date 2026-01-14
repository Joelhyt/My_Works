#include "PlayerFilterProxyModel.h"
#include <QModelIndex>

PlayerFilterProxyModel::PlayerFilterProxyModel(QObject* parent)
    : QSortFilterProxyModel(parent)
{
}

void PlayerFilterProxyModel::setNumericFilter(int column, double minVal, double maxVal, bool enabled) {
    m_numericEnabled = enabled;
    m_numericColumn = column;
    m_min = minVal;
    m_max = maxVal;
    invalidateFilter();
}

void PlayerFilterProxyModel::setTextFilter(int column, const QString& text, bool enabled) {
    m_textEnabled = enabled;
    m_textColumn = column;
    m_text = text;
    invalidateFilter();
}

bool PlayerFilterProxyModel::filterAcceptsRow(int sourceRow, const QModelIndex& sourceParent) const {
    const QAbstractItemModel* src = sourceModel();

    // --- Text filter ---
    if (m_textEnabled && m_textColumn >= 0) {
        QModelIndex index = src->index(sourceRow, m_textColumn, sourceParent);
        QString value = src->data(index).toString();
        if (!value.contains(m_text, Qt::CaseInsensitive))
            return false;
    }

    // --- Numeric filter ---
    if (m_numericEnabled && m_numericColumn >= 0) {
        QModelIndex index = src->index(sourceRow, m_numericColumn, sourceParent);
        bool ok;
        double num = src->data(index).toDouble(&ok);
        if (!ok) return false;
        if (num < m_min || num > m_max)
            return false;
    }

    return true;
}

bool PlayerFilterProxyModel::lessThan(const QModelIndex &left, const QModelIndex &right) const {
    QVariant leftData = sourceModel()->data(left);
    QVariant rightData = sourceModel()->data(right);

    // Try numeric comparison first
    bool leftOk, rightOk;
    double leftNum = leftData.toDouble(&leftOk);
    double rightNum = rightData.toDouble(&rightOk);

    if (leftOk && rightOk) {
        return leftNum < rightNum;
    }

    QString leftStr = leftData.toString();
    QString rightStr = rightData.toString();
    return QString::localeAwareCompare(leftStr, rightStr) < 0;
}
