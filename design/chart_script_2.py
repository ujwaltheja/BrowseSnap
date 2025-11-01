import plotly.express as px
import plotly.graph_objects as go
import pandas as pd

# Data from the instructions 
phases_data = {
    'name': ['Foundation & WebSocket', 'Device Pairing', 'Mobile App', 'TV App', 'Advanced Features'],
    'hours': [28, 44, 104, 140, 36],
    'instructed_percentage': [11, 18, 41, 55, 14]  # Percentages from instructions
}

# Create DataFrame
df = pd.DataFrame(phases_data)

# Abbreviate names to fit 15 character limit while keeping key info
df['short_name'] = ['Foundation', 'Device Pair', 'Mobile App', 'TV App', 'Advanced']

# Create custom text with instructed percentages
df['custom_text'] = df['short_name'] + '<br>' + df['instructed_percentage'].astype(str) + '%'

# Use the brand colors from the theme
brand_colors = ['#1FB8CD', '#DB4545', '#2E8B57', '#5D878F', '#D2BA4C']

# Create donut chart using hours for slice sizes but custom text for display
fig = go.Figure(data=[go.Pie(
    labels=df['short_name'],
    values=df['hours'],
    hole=0.4,
    text=df['custom_text'],
    textinfo='text',
    textposition='inside',
    marker_colors=brand_colors,
    hovertemplate='<b>%{label}</b><br>Hours: %{value}<br>Target: %{customdata}%<extra></extra>',
    customdata=df['instructed_percentage']
)])

# Update layout
fig.update_layout(
    title='BrowseSnap Effort Breakdown',
    uniformtext_minsize=14, 
    uniformtext_mode='hide',
    annotations=[
        dict(text=f'Total: 352h<br>0% Complete', x=0.5, y=0.5, font_size=14, showarrow=False)
    ]
)

# Save the chart
fig.write_image("chart.png")
fig.write_image("chart.svg", format="svg")

print("Chart updated successfully!")
print(f"Total hours: {df['hours'].sum()}")
print("Breakdown showing instructed percentages:")
for _, row in df.iterrows():
    print(f"- {row['name']}: {row['hours']}h (showing as {row['instructed_percentage']}%)")
print("Current completion: 0%")