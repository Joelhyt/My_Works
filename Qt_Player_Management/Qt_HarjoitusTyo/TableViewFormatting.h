#ifndef TABLEVIEWFORMATTING_H
#define TABLEVIEWFORMATTING_H

#pragma once
#include <QStandardItemModel>
#include <QRegularExpression>
#include <QMessageBox>

class TableViewFormatting : public QStandardItemModel {
    Q_OBJECT
public:
    using QStandardItemModel::QStandardItemModel;

    bool setData(const QModelIndex& index, const QVariant& value, int role = Qt::EditRole) override {
        if (role == Qt::EditRole) {
            if (index.column() == 0) { // NAME column
                QString name = value.toString().trimmed();
                QRegularExpression re("^[\\p{L} .'-]+$");
                if (name.isEmpty() || !re.match(name).hasMatch()) {
                    QMessageBox::warning(nullptr, "Invalid Input", "Name must contain only letters.");
                    return false;
                }
            }
            else if (index.column() >= 1) { // Attribute columns
                bool ok;
                value.toString().toDouble(&ok);
                if (!ok) {
                    QMessageBox::warning(nullptr, "Invalid Input", "This field accepts numbers only.");
                    return false;
                }
                if (value.toString().toDouble() < 0 && (index.column() == 1 || index.column() == 2 || index.column() == 3)) {
                    QMessageBox::warning(nullptr, "Invalid Input", "Age, height and weight values can't be negative");
                    return false;
                }
            }
        }
        return QStandardItemModel::setData(index, value, role);
    }
};

#endif // TABLEVIEWFORMATTING_H
