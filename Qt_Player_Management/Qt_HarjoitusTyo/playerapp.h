#ifndef PLAYERAPP_H
#define PLAYERAPP_H

#include <QWidget>
#include <QStandardItemModel>
#include <QTableView>
#include <QSpinBox>
#include <QComboBox>
#include <QLineEdit>
#include <QCheckBox>

#include "PlayerFilterProxyModel.h"

class PlayerApp : public QWidget {
    Q_OBJECT

public:
    explicit PlayerApp(QWidget* parent = nullptr);

private slots:
    void addPlayer();
    void removePlayer();
    void addAttribute();
    void applyNumericFilter();
    void applyTextFilter();
    void debugPrint();

private:
    QStandardItemModel* createModel();

    QStandardItemModel* model;
    PlayerFilterProxyModel* proxy;
    QTableView* view;

    // Numeric filter widgets
    QComboBox* numericColumnCombo;
    QSpinBox* minSpin;
    QSpinBox* maxSpin;
    QCheckBox* numericEnableCheck;

    // Text filter widgets
    QComboBox* textColumnCombo;
    QLineEdit* textEdit;
    QCheckBox* textEnableCheck;
};

#endif // PLAYERAPP_H
