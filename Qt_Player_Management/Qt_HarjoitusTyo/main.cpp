#include <QApplication>
#include "PlayerApp.h"

int main(int argc, char* argv[]) {
    QApplication app(argc, argv);
    PlayerApp window;
    window.show();
    return app.exec();
}
