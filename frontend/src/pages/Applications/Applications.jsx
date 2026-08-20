import "./Applications.css";

function Applications() {
    return (
        <div className="applications-page">

            <h1>My Applications</h1>

            <p className="applications-subtitle">
                Track the jobs you have applied for.
            </p>

            <div className="application-card">

                <div>
                    <h2>Java Developer</h2>
                    <p>Tech Solutions</p>
                    <p>📍 Bangalore</p>
                </div>

                <div className="application-status">
                    <span>APPLIED</span>
                    <p>Applied on: 18-08-2026</p>
                </div>

            </div>

        </div>
    );
}

export default Applications;