#include "AddPlayerDialog.h"
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QLineEdit>
#include <QSpinBox>
#include <QDialogButtonBox>

AddPlayerDialog::AddPlayerDialog(QWidget* parent)
    : QDialog(parent)
{
    setWindowTitle("Add New Player");

    nameEdit = new QLineEdit;
    ageSpin = new QSpinBox;
    heightSpin = new QSpinBox;
    weightSpin = new QSpinBox;

    ageSpin->setRange(0, 120);
    heightSpin->setRange(100, 250);
    weightSpin->setRange(30, 200);

    auto* formLayout = new QVBoxLayout;
    auto addRow = [&](const QString& label, QWidget* widget) {
        auto* row = new QHBoxLayout;
        row->addWidget(new QLabel(label));
        row->addWidget(widget);
        formLayout->addLayout(row);
    };

    addRow("Name:", nameEdit);
    addRow("Age:", ageSpin);
    addRow("Height (cm):", heightSpin);
    addRow("Weight (kg):", weightSpin);

    auto* buttons = new QDialogButtonBox(QDialogButtonBox::Ok | QDialogButtonBox::Cancel);
    connect(buttons, &QDialogButtonBox::accepted, this, &AddPlayerDialog::accept);
    connect(buttons, &QDialogButtonBox::rejected, this, &AddPlayerDialog::reject);

    formLayout->addWidget(buttons);
    setLayout(formLayout);
}

QString AddPlayerDialog::name() const { return nameEdit->text(); }
int AddPlayerDialog::age() const { return ageSpin->value(); }
int AddPlayerDialog::height() const { return heightSpin->value(); }
int AddPlayerDialog::weight() const { return weightSpin->value(); }
