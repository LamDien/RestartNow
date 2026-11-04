import math
import subprocess
import os

# Canvas 200x200, center (100, 100)
cx, cy = 100.0, 100.0

def polar_to_cart(r, angle_deg):
    rad = math.radians(angle_deg)
    return cx + r * math.cos(rad), cy + r * math.sin(rad)

def create_svg(filename, arrow_r_out, arrow_r_in, arrow_start_deg, arrow_end_deg, head_w, head_h, p_r_out, p_r_in, p_gap_deg, bar_w, bar_h, bar_top):
    # In SVG (Y down):
    # Increasing angle is CLOCKWISE.
    # Decreasing angle is COUNTER-CLOCKWISE.
    # Top is -90° (or 270°).
    # Left is 180° (-180°).
    # Bottom is 90°.
    # Right is 0°.
    
    # Outer arrow:
    # Arc goes CCW from arrow_start_deg to arrow_end_deg (e.g. from 30° to 270°, meaning decreasing angle: 30° -> 0° -> -90° / 270°).
    # Wait, in SVG path A (arc):
    # A rx ry x-axis-rotation large-arc-flag sweep-flag x y
    # sweep-flag: 1 for clockwise (positive angle), 0 for counter-clockwise (negative angle).
    
    # Let's define the outer arrow:
    # Center = (100, 100).
    # We want CCW arrow:
    # Arrow tail starts at e.g. angle +20° (bottom right) or +45°.
    # Outer arc travels CCW (sweep-flag = 0) to angle -70° (top, slightly before 12 o'clock).
    # At angle -70° (or -90° = 12 o'clock), arrowhead is attached pointing CCW (tangent pointing left).
    # Let's say arrow head base is at X = 100, Y = 100 - r_mid.
    # Better yet, let's make an explicit polygon/arc for the arrow!
    
    svg_content = f"""<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 200 200">
    <rect width="200" height="200" fill="#0A0F1D" />
    
    <!-- Outer CCW Arrow (To gap 2) -->
    <!-- Arc outer from tail to head, then arrowhead, then inner arc back to tail -->
"""
    return svg_content

print("Script template ready")
