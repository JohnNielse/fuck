/*
 * Copyright 2019 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shade.platform.ui.controls;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import com.shade.util.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Rectangle2D;

class ListCellTitledBorder implements Border {
    private final JList<?> list;
    private final String title;

    ListCellTitledBorder(@NotNull JList<?> list, @NotNull String title) {
        this.list = list;
        this.title = title;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int height = c.getFontMetrics(list.getFont()).getHeight();
        return new Insets(height, 0, 0, 0);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        final FontMetrics fm = c.getFontMetrics(list.getFont());
        final int titleWidth = fm.stringWidth(title);
        final int titleHeight = fm.getHeight();

        // fill background
        g.setColor(list.getBackground());
        g.fillRect(x, y, width, titleHeight);

        final Graphics2D g2 = (Graphics2D) g.create();

        try {
            FlatUIUtils.setRenderingHints(g2);

            g2.setColor(UIManager.getColor("Label.disabledForeground"));

            final int gap = UIScale.scale(4);
            final int sepWidth = (width - titleWidth) / 2 - gap - gap;

            // paint separator lines
            if (sepWidth > 0) {
                int sy = y + Math.round(titleHeight / 2f);
                float sepHeight = UIScale.scale((float) 1);

                g2.fill(new Rectangle2D.Float(x + gap, sy, sepWidth, sepHeight));
                g2.fill(new Rectangle2D.Float(x + width - gap - sepWidth, sy, sepWidth, sepHeight));
            }

            // draw title
            int xt = x + ((width - titleWidth) / 2);
            int yt = y + fm.getAscent();

            FlatUIUtils.drawString(list, g2, title, xt, yt);
        } finally {
            g2.dispose();
        }
    }
}