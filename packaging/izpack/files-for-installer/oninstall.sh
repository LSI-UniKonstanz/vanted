#!/bin/sh
rm  $HOME/.local/share/applications/vanted*
rm  $HOME/.local/share/applications/Vanted*
cp $INSTALL_PATH/vanted.desktop $HOME/.local/share/applications/
chmod +x $INSTALL_PATH/starter.sh
touch $INSTALL_PATH/installationtimestamp
