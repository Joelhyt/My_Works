#include "AddAttributeDialog.h"
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QLineEdit>
#include <QDialogButtonBox>

AddAttributeDialog::AddAttributeDialog(QWidget* parent)
    : QDialog(parent)
{
    setWindowTitle("Add New Attribute");

    nameEdit = new QLineEdit;
    defaultValueEdit = new QLineEdit;

    auto* formLayout = new QVBoxLayout;
    auto addRow = [&](const QString& label, QWidget* widget) {
        auto* row = new QHBoxLayout;
        row->addWidget(new QLabel(label));
        row->addWidget(widget);
        formLayout->addLayout(row);
    };

    addRow("Attribute name:", nameEdit);
    addRow("Default value:", defaultValueEdit);

    auto* buttons = new QDialogButtonBox(QDialogButtonBox::Ok | QDialogButtonBox::Cancel);
    connect(buttons, &QDialogButtonBox::accepted, this, &AddAttributeDialog::accept);
    connect(buttons, &QDialogButtonBox::rejected, this, &AddAttributeDialog::reject);

    formLayout->addWidget(buttons);
    setLayout(formLayout);
}

QString AddAttributeDialog::attributeName() const {
    return nameEdit->text();
}

QString AddAttributeDialog::defaultValue() const {
    return defaultValueEdit->text();
}
