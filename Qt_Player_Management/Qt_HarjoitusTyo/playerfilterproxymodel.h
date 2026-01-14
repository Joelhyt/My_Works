#ifndef PLAYERFILTERPROXYMODEL_H
#define PLAYERFILTERPROXYMODEL_H

#include <QSortFilterProxyModel>
#include <QString>

class PlayerFilterProxyModel : public QSortFilterProxyModel {
    Q_OBJECT
public:
    explicit PlayerFilterProxyModel(QObject* parent = nullptr);

    void setNumericFilter(int column, double minVal, double maxVal, bool enabled);
    void setTextFilter(int column, const QString& text, bool enabled);

protected:
    bool filterAcceptsRow(int sourceRow, const QModelIndex& sourceParent) const override;
    bool lessThan(const QModelIndex &left, const QModelIndex &right) const override;

private:
    // Numeric filtering
    bool m_numericEnabled = false;
    int m_numericColumn = -1;
    double m_min = 0.0;
    double m_max = 999999.0;

    // Text filtering
    bool m_textEnabled = false;
    int m_textColumn = -1;
    QString m_text;
};

#endif // PLAYERFILTERPROXYMODEL_H
