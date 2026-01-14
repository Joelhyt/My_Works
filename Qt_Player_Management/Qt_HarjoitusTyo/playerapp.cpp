#include "PlayerApp.h"
#include "AddPlayerDialog.h"
#include "AddAttributeDialog.h"
#include "TableViewFormatting.h"
#include <QPushButton>
#include <QCheckBox>
#include <QHBoxLayout>
#include <QVBoxLayout>
#include <QLabel>
#include <QMessageBox>
#include <QHeaderView>
#include <QDebug>

PlayerApp::PlayerApp(QWidget* parent)
    : QWidget(parent)
{
    model = createModel();
    proxy = new PlayerFilterProxyModel(this);
    proxy->setSourceModel(model);

    view = new QTableView;
    view->setModel(proxy);
    view->setSortingEnabled(true);
    view->resizeColumnsToContents();
    view->verticalHeader()->setVisible(false);

    // --- Buttons ---
    auto* addPlayerBtn = new QPushButton("Add player");
    auto* removePlayerBtn = new QPushButton("Remove player");
    auto* addAttrBtn = new QPushButton("Add attribute");
    auto* debugBtn = new QPushButton("debug print");

    // --- Numeric filter controls ---
    numericEnableCheck = new QCheckBox("Enable numeric filter");
    numericColumnCombo = new QComboBox;
    numericColumnCombo->addItems({"AGE", "HEIGHT", "WEIGHT"});
    minSpin = new QSpinBox;
    maxSpin = new QSpinBox;
    minSpin->setRange(-999, 999);
    maxSpin->setRange(-999, 999);
    minSpin->setValue(0);
    maxSpin->setValue(999);

    // --- Text filter controls ---
    textEnableCheck = new QCheckBox("Enable text filter");
    textColumnCombo = new QComboBox;
    textColumnCombo->addItems({"NAME"});
    textEdit = new QLineEdit;
    textEdit->setPlaceholderText("Enter text...");

    // --- Layouts ---
    auto* numericLayout = new QHBoxLayout;
    numericLayout->addWidget(numericEnableCheck);
    numericLayout->addWidget(new QLabel("Column:"), 0, Qt::AlignRight);
    numericLayout->addWidget(numericColumnCombo);
    numericLayout->addWidget(new QLabel("Min:"), 0, Qt::AlignRight);
    numericLayout->addWidget(minSpin);
    numericLayout->addWidget(new QLabel("Max:"), 0, Qt::AlignRight);
    numericLayout->addWidget(maxSpin);


    auto* textLayout = new QHBoxLayout;
    textLayout->addWidget(textEnableCheck);
    textLayout->addWidget(new QLabel("Column:"), 0, Qt::AlignRight);
    textLayout->addWidget(textColumnCombo);
    textLayout->addWidget(textEdit);

    auto* topLayout = new QHBoxLayout;
    topLayout->addWidget(addPlayerBtn);
    topLayout->addWidget(removePlayerBtn);
    topLayout->addWidget(addAttrBtn);
    topLayout->addWidget(debugBtn);

    auto* mainLayout = new QVBoxLayout(this);
    mainLayout->addLayout(topLayout);
    mainLayout->addLayout(textLayout);
    mainLayout->addLayout(numericLayout);
    mainLayout->addWidget(view);
    setLayout(mainLayout);

    // --- Connections ---
    connect(addPlayerBtn, &QPushButton::clicked, this, &PlayerApp::addPlayer);
    connect(removePlayerBtn, &QPushButton::clicked, this, &PlayerApp::removePlayer);
    connect(addAttrBtn, &QPushButton::clicked, this, &PlayerApp::addAttribute);
    connect(debugBtn, &QPushButton::clicked, this, &PlayerApp::debugPrint);

    connect(numericEnableCheck, &QCheckBox::toggled, this, &PlayerApp::applyNumericFilter);
    connect(minSpin, qOverload<int>(&QSpinBox::valueChanged), this, &PlayerApp::applyNumericFilter);
    connect(maxSpin, qOverload<int>(&QSpinBox::valueChanged), this, &PlayerApp::applyNumericFilter);
    connect(numericColumnCombo, &QComboBox::currentIndexChanged, this, &PlayerApp::applyNumericFilter);

    connect(textEnableCheck, &QCheckBox::toggled, this, &PlayerApp::applyTextFilter);
    connect(textEdit, &QLineEdit::textChanged, this, &PlayerApp::applyTextFilter);
    connect(textColumnCombo, &QComboBox::currentIndexChanged, this, &PlayerApp::applyTextFilter);

    setWindowTitle("Player Manager");
    resize(900, 500);
}

QStandardItemModel* PlayerApp::createModel() {
    auto* m = new TableViewFormatting(this);
    m->setHorizontalHeaderLabels({"NAME", "AGE", "HEIGHT", "WEIGHT"});

    QList<QList<QVariant>> players = { // Test data
        {"Pekka", 33, 179, 88},
        {"Johansson", 24, 186, 82},
        {"Juuso", 27, 170, 71},
        {"Pena", 45, 168, 92},
        {"Make", 39, 174, 85}

    };

    for (const auto& row : players) {
        QList<QStandardItem*> items;
        for (const auto& value : row)
            items << new QStandardItem(value.toString());
        m->appendRow(items);
    }

    return m;
}

void PlayerApp::debugPrint() {
    if (!model)
        return;

    for (int row = 0; row < model->rowCount(); ++row) {
        QStringList rowValues;
        for (int col = 0; col < model->columnCount(); ++col) {
            QModelIndex index = model->index(row, col);
            rowValues << model->data(index).toString();
        }
        qDebug() << rowValues.join(", ");
    }
}

void PlayerApp::addPlayer() {
    AddPlayerDialog dialog(this);

    if (dialog.exec() == QDialog::Accepted) {
        QString name = dialog.name().trimmed();

        if (name.isEmpty()) {
            QMessageBox::warning(this, "Invalid Input", "Name cannot be empty.");
            return;
        }

        QRegularExpression re("^[\\p{L} .'-]+$");
        if (!re.match(name).hasMatch()) {
            QMessageBox::warning(this, "Invalid Input", "Name cannot contain numbers.");
            return;
        }

        QList<QStandardItem*> items;
        items << new QStandardItem(dialog.name())
              << new QStandardItem(QString::number(dialog.age()))
              << new QStandardItem(QString::number(dialog.height()))
              << new QStandardItem(QString::number(dialog.weight()));


        // fill defaul values
        while (items.size() < model->columnCount())
            items << new QStandardItem("");

        model->appendRow(items);
    }
}

void PlayerApp::removePlayer() {
    QItemSelectionModel* selection = view->selectionModel();
    if (!selection->hasSelection()) {
        QMessageBox::information(this, "No Selection", "Please select a player to remove.");
        return;
    }

    QModelIndex proxyIndex = selection->currentIndex();
    if (!proxyIndex.isValid())
        return;

    QModelIndex sourceIndex = proxy->mapToSource(proxyIndex);
    int row = sourceIndex.row();

    QString playerName = model->item(row, 0)->text();
    auto reply = QMessageBox::question(this, "Confirm Delete",
    QString("Remove player '%1'?").arg(playerName),
    QMessageBox::Yes | QMessageBox::No);

    if (reply == QMessageBox::Yes)
        model->removeRow(row);
}

void PlayerApp::addAttribute() {
    AddAttributeDialog dialog(this);
    if (dialog.exec() == QDialog::Accepted) {
        QString name = dialog.attributeName().trimmed();
        QString defVal = dialog.defaultValue();

        if (defVal.isEmpty()) {
            defVal = "0";
        }

        if (name.isEmpty()) {
            QMessageBox::warning(this, "Invalid Input", "Attribute name cannot be empty.");
            return;
        }

        QRegularExpression re("^[\\p{L} .'-]+$");
        if (!re.match(name).hasMatch()) {
            QMessageBox::warning(this, "Invalid Input", "Attribute name cannot contain numbers.");
            return;
        }

        bool ok = false;
        defVal.toDouble(&ok);
        if (!ok) {
            QMessageBox::warning(this, "Invalid Input", "Default value must be numeric.");
            return;
        }

        int col = model->columnCount();
        model->insertColumn(col);
        model->setHeaderData(col, Qt::Horizontal, name);

        // Set default value
        for (int row = 0; row < model->rowCount(); ++row)
            model->setData(model->index(row, col), defVal);

        numericColumnCombo->addItem(name);

        view->setSortingEnabled(false);
        view->setSortingEnabled(true);
        proxy->invalidate();
    }
}

void PlayerApp::applyNumericFilter() {
    if (numericEnableCheck->isChecked()) {
        int col = numericColumnCombo->currentIndex() + 1; // skip NAME
        proxy->setNumericFilter(col, minSpin->value(), maxSpin->value(), true);
    } else {
        proxy->setNumericFilter(-1, 0, 0, false);
    }
}

void PlayerApp::applyTextFilter() {
    if (textEnableCheck->isChecked()) {
        int col = textColumnCombo->currentIndex(); // NAME
        proxy->setTextFilter(col, textEdit->text(), true);
    } else {
        proxy->setTextFilter(-1, "", false);
    }
}
