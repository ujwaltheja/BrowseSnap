import plotly.graph_objects as go
import plotly.express as px
import json

# Parse the provided data
data = {
    "phases": [
        {"name": "Foundation & WebSocket", "start": 1, "duration": 2, "category": "Core Infrastructure", "status": "Not Started", "components": ["WebSocket Client", "WebSocket Server", "Command Schema"]}, 
        {"name": "Device Pairing", "start": 2, "duration": 2, "category": "Connection", "status": "Not Started", "components": ["QR Generation", "QR Scanning", "PIN Fallback"]}, 
        {"name": "Mobile App", "start": 3, "duration": 3, "category": "Mobile", "status": "Not Started", "components": ["Search Module", "Command Sender", "History Module", "Mobile UI"]}, 
        {"name": "TV App", "start": 5, "duration": 3, "category": "TV", "status": "Not Started", "components": ["WebSocket Server", "Command Handler", "WebView", "Video Player", "TV UI"]}, 
        {"name": "Advanced Features", "start": 7, "duration": 2, "category": "Polish", "status": "Not Started", "components": ["Error Handling", "Security (WSS)", "Testing"]}
    ], 
    "total_weeks": 9, 
    "team_size": "2-3 developers", 
    "estimated_hours": 252
}

# Create the figure
fig = go.Figure()

# Define colors for each category using the brand colors
category_colors = {
    "Core Infrastructure": "#1FB8CD",  # Strong cyan
    "Connection": "#DB4545",           # Bright red
    "Mobile": "#2E8B57",               # Sea green
    "TV": "#5D878F",                   # Cyan
    "Polish": "#D2BA4C"                # Moderate yellow
}

# Track which categories have been added to legend
added_categories = set()

# Add bars for each phase
for i, phase in enumerate(data["phases"]):
    # Calculate end week
    end_week = phase["start"] + phase["duration"] - 1
    
    # Create hover text with components
    components_text = ", ".join(phase["components"])
    hover_text = f"<b>{phase['name']}</b><br>Week {phase['start']}-{end_week}<br>Duration: {phase['duration']} weeks<br>Category: {phase['category']}<br>Components: {components_text}"
    
    # Add horizontal bar with correct positioning
    fig.add_trace(go.Bar(
        y=[phase["name"]],
        x=[phase["duration"]],
        base=[phase["start"]],  # Start position on x-axis
        orientation='h',
        name=phase["category"],
        marker_color=category_colors[phase["category"]],
        hovertemplate=hover_text + "<extra></extra>",
        showlegend=phase["category"] not in added_categories
    ))
    
    # Add text annotation for week range on each bar
    fig.add_annotation(
        x=phase["start"] + phase["duration"]/2,  # Center of the bar
        y=i,  # Phase position
        text=f"Week {phase['start']}-{end_week}",
        showarrow=False,
        font=dict(color="white", size=11),
        xanchor="center",
        yanchor="middle"
    )
    
    added_categories.add(phase["category"])

# Add vertical line to show current status (Week 0)
fig.add_vline(
    x=0.5, 
    line_dash="dash", 
    line_color="gray",
    annotation_text="Current (Week 0)",
    annotation_position="top"
)

# Update layout
fig.update_layout(
    title="BrowseSnap Dev Timeline",
    xaxis_title="Week",
    yaxis_title="Development Phase",
    xaxis=dict(
        tickmode='linear',
        tick0=0,
        dtick=1,
        range=[0, 9],
        tickvals=list(range(0, 9)),
        ticktext=[f"Week {i}" for i in range(0, 9)]
    ),
    yaxis=dict(
        categoryorder='array',
        categoryarray=[phase["name"] for phase in reversed(data["phases"])]
    ),
    barmode='overlay',
    legend=dict(
        orientation='v',
        yanchor='top',
        y=1,
        xanchor='left',
        x=1.02
    )
)

# Update traces to remove clip on axis
fig.update_traces(cliponaxis=False)

# Save the chart
fig.write_image("gantt_chart.png")
fig.write_image("gantt_chart.svg", format="svg")

fig.show()