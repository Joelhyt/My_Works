#ifndef ADDPLAYERDIALOG_H
#define ADDPLAYERDIALOG_H

#include <QDialog>

class QLineEdit;
class QSpinBox;

class AddPlayerDialog : public QDialog {
    Q_OBJECT
public:
    explicit AddPlayerDialog(QWidget* parent = nullptr);

    QString name() const;
    int age() const;
    int height() const;
    int weight() const;

private:
    QLineEdit* nameEdit;
    QSpinBox* ageSpin;
    QSpinBox* heightSpin;
    QSpinBox* weightSpin;
};

#endif // ADDPLAYERDIALOG_H
