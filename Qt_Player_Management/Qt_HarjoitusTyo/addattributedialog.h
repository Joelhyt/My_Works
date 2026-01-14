#ifndef ADDATTRIBUTEDIALOG_H
#define ADDATTRIBUTEDIALOG_H

#include <QDialog>

class QLineEdit;

class AddAttributeDialog : public QDialog {
    Q_OBJECT
public:
    explicit AddAttributeDialog(QWidget* parent = nullptr);

    QString attributeName() const;
    QString defaultValue() const;

private:
    QLineEdit* nameEdit;
    QLineEdit* defaultValueEdit;
};

#endif // ADDATTRIBUTEDIALOG_H
